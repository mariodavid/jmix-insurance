package com.insurance.common.test_support_ui.architecture.ui;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewDescriptor;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses Jmix view and fragment XML descriptors so ArchUnit rules can verify that controller
 * annotations point to descriptor files and element IDs that actually exist.
 */
public class JmixUiDescriptorScanner {

  private static final Map<String, Set<String>> xmlIdsCache = new ConcurrentHashMap<>();

  public static String getDescriptorPath(JavaClass clazz) {
    if (clazz.isAnnotatedWith(ViewDescriptor.class)) {
      ViewDescriptor descriptor = clazz.getAnnotationOfType(ViewDescriptor.class);
      return firstNonBlank(descriptor.path(), descriptor.value());
    }
    if (clazz.isAnnotatedWith(FragmentDescriptor.class)) {
      FragmentDescriptor descriptor = clazz.getAnnotationOfType(FragmentDescriptor.class);
      return descriptor.value();
    }
    return null;
  }

  public static boolean descriptorFileExists(JavaClass clazz) {
    String path = getDescriptorPath(clazz);
    if (path == null) {
      return false;
    }
    String resourcePath = descriptorResourcePath(clazz, path);
    try {
      URL resource = clazz.reflect().getClassLoader().getResource(resourcePath);
      return resource != null;
    } catch (Exception e) {
      return false;
    }
  }

  public static Set<String> getXmlIds(JavaClass clazz) {
    String path = getDescriptorPath(clazz);
    if (path == null) {
      return Collections.emptySet();
    }
    String resourcePath = descriptorResourcePath(clazz, path);
    return xmlIdsCache.computeIfAbsent(
        resourcePath,
        key -> {
          try (InputStream is = clazz.reflect().getClassLoader().getResourceAsStream(key)) {
            if (is == null) {
              return Collections.emptySet();
            }
            return parseXmlIds(is);
          } catch (Exception e) {
            return Collections.emptySet();
          }
        });
  }

  public static List<String> validateBindings(JavaClass clazz) {
    if (getDescriptorPath(clazz) == null) {
      return Collections.emptyList();
    }
    if (!descriptorFileExists(clazz)) {
      return Collections.singletonList(
          String.format("Descriptor file not found for class %s", clazz.getName()));
    }

    List<String> errors = new ArrayList<>();
    Set<String> xmlIds = getXmlIds(clazz);

    validateEditedEntityContainer(clazz, xmlIds, errors);
    validateLookupComponent(clazz, xmlIds, errors);
    validateSubscribeMethods(clazz, xmlIds, errors);
    validateInstallMethods(clazz, xmlIds, errors);

    return errors;
  }

  private static void validateEditedEntityContainer(
      JavaClass clazz, Set<String> xmlIds, List<String> errors) {
    if (!clazz.isAnnotatedWith(EditedEntityContainer.class)) {
      return;
    }
    EditedEntityContainer annotation = clazz.getAnnotationOfType(EditedEntityContainer.class);
    if (hasText(annotation.value()) && !xmlIds.contains(annotation.value())) {
      errors.add(
          String.format(
              "@EditedEntityContainer(\"%s\") not found in XML descriptor", annotation.value()));
    }
  }

  private static void validateLookupComponent(
      JavaClass clazz, Set<String> xmlIds, List<String> errors) {
    if (!clazz.isAnnotatedWith(LookupComponent.class)) {
      return;
    }
    LookupComponent annotation = clazz.getAnnotationOfType(LookupComponent.class);
    if (hasText(annotation.value()) && !xmlIds.contains(annotation.value())) {
      errors.add(
          String.format(
              "@LookupComponent(\"%s\") not found in XML descriptor", annotation.value()));
    }
  }

  private static void validateSubscribeMethods(
      JavaClass clazz, Set<String> xmlIds, List<String> errors) {
    for (JavaMethod method : clazz.getMethods()) {
      if (!method.isAnnotatedWith(Subscribe.class)) {
        continue;
      }
      Subscribe subscribe = method.getAnnotationOfType(Subscribe.class);
      String targetId = firstNonBlank(subscribe.value(), subscribe.id());
      if (hasText(targetId)) {
        validateSubscribeTarget(method, targetId, xmlIds, errors);
      }
    }
  }

  private static void validateInstallMethods(
      JavaClass clazz, Set<String> xmlIds, List<String> errors) {
    for (JavaMethod method : clazz.getMethods()) {
      if (!method.isAnnotatedWith(Install.class)) {
        continue;
      }
      Install install = method.getAnnotationOfType(Install.class);
      if (hasText(install.to()) && !xmlIds.contains(install.to())) {
        errors.add(
            String.format(
                "Method %s: @Install target \"to = %s\" not found in XML descriptor",
                method.getName(), install.to()));
      }
    }
  }

  private static void validateSubscribeTarget(
      JavaMethod method, String targetId, Set<String> xmlIds, List<String> errors) {
    for (String segment : targetId.split("\\.")) {
      if (!xmlIds.contains(segment)) {
        errors.add(
            String.format(
                "Method %s: @Subscribe target ID segment \"%s\" (from \"%s\") not found in XML descriptor",
                method.getName(), segment, targetId));
      }
    }
  }

  private static Set<String> parseXmlIds(InputStream is) throws Exception {
    Set<String> ids = new HashSet<>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(is);
    NodeList nodes = doc.getElementsByTagName("*");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element element = (Element) nodes.item(i);
      if (element.hasAttribute("id")) {
        ids.add(element.getAttribute("id"));
      }
    }
    return ids;
  }

  private static String descriptorResourcePath(JavaClass clazz, String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return clazz.getPackageName().replace('.', '/') + "/" + path;
  }

  private static String firstNonBlank(String first, String second) {
    return hasText(first) ? first : second;
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
