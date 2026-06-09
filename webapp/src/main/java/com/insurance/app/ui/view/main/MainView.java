package com.insurance.app.ui.view.main;

import com.google.common.base.Strings;
import com.insurance.security.core.entity.User;
import com.insurance.theme.ModuleTheme;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.horizontalmenu.HorizontalMenu;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@Route("")
@ViewController(id = "app_MainView")
@ViewDescriptor(path = "main-view.xml")
public class MainView extends StandardMainView {

  private final Messages messages;
  private final UiComponents uiComponents;
  private final CurrentUserSubstitution currentUserSubstitution;
  private final List<ModuleTheme> moduleThemes;

  @Autowired
  public MainView(
      Messages messages,
      UiComponents uiComponents,
      CurrentUserSubstitution currentUserSubstitution,
      @Autowired(required = false) List<ModuleTheme> moduleThemes) {
    this.messages = messages;
    this.uiComponents = uiComponents;
    this.currentUserSubstitution = currentUserSubstitution;
    this.moduleThemes = moduleThemes;
  }

  @Override
  protected void updateTitle() {
    super.updateTitle();

    String viewTitle = getTitleFromOpenedView();
    io.jmix.flowui.component.UiComponentUtils.findComponent(getContent(), "viewHeaderBox")
        .ifPresent(component -> component.setVisible(!Strings.isNullOrEmpty(viewTitle)));

    // Clean up previous theme classes dynamically
    getElement().getClassList().stream()
        .filter(className -> className.endsWith("-theme"))
        .toList()
        .forEach(className -> getElement().getClassList().remove(className));

    Component activeView = getContent().getContent();
    String viewId = getActiveViewId(activeView);

    ModuleTheme activeTheme = null;
    if (viewId != null && moduleThemes != null) {
      activeTheme =
          moduleThemes.stream()
              .filter(theme -> viewId.startsWith(theme.getModulePrefix() + "_"))
              .findFirst()
              .orElse(null);
    }

    if (activeTheme != null) {
      getElement().getClassList().add(activeTheme.getThemeClass());
    }

    io.jmix.flowui.component.UiComponentUtils.findComponent(getContent(), "horizontalMenu")
        .ifPresent(
            component -> {
              if (component instanceof HorizontalMenu menu) {
                highlightActiveMenuItem(menu, viewId);
              }
            });

    final ModuleTheme finalTheme = activeTheme;
    io.jmix.flowui.component.UiComponentUtils.findComponent(getContent(), "viewHeaderIconContainer")
        .ifPresent(
            component -> {
              Div container = (Div) component;
              container.removeAll();
              if (finalTheme != null) {
                Component icon = finalTheme.getHeaderIcon();
                if (icon != null) {
                  container.add(icon);
                  container.setVisible(true);
                  return;
                }
              }
              container.setVisible(false);
            });
  }

  private void highlightActiveMenuItem(HorizontalMenu menu, String viewId) {
    if (viewId == null) {
      return;
    }
    String prefix = viewId.contains("_") ? viewId.substring(0, viewId.indexOf('_')) : viewId;

    for (HorizontalMenu.AbstractMenuItem<?> item : menu.getMenuItems()) {
      item.getElement().getClassList().remove("menu-item-active");

      String itemId = item.getId().orElse("");
      if (!itemId.isEmpty()) {
        String itemPrefix =
            itemId.contains("_") ? itemId.substring(0, itemId.indexOf('_')) : itemId;
        if (itemPrefix.equals(prefix)) {
          item.getElement().getClassList().add("menu-item-active");
        }
      }
    }
  }

  private String getActiveViewId(Component activeView) {
    String activeViewId = null;
    if (activeView != null) {
      ViewController annotation = activeView.getClass().getAnnotation(ViewController.class);
      if (annotation != null) {
        activeViewId = annotation.id().isEmpty() ? annotation.value() : annotation.id();
      }
    }
    return activeViewId;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  @Install(to = "userMenu", subject = "buttonRenderer")
  private Component userMenuButtonRenderer(final UserDetails userDetails) {
    if (!(userDetails instanceof User user)) {
      return null;
    }

    String userName = generateUserName(user);

    Div content = uiComponents.create(Div.class);
    content.setClassName("user-menu-button-content");

    Avatar avatar = createAvatar(userName);

    Span name = uiComponents.create(Span.class);
    name.setText(userName);
    name.setClassName("user-menu-text");

    content.add(avatar, name);

    if (isSubstituted(user)) {
      Span subtext = uiComponents.create(Span.class);
      subtext.setText(messages.getMessage("userMenu.substituted"));
      subtext.setClassName("user-menu-subtext");

      content.add(subtext);
    }

    return content;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  @Install(to = "userMenu", subject = "headerRenderer")
  private Component userMenuHeaderRenderer(final UserDetails userDetails) {
    if (!(userDetails instanceof User user)) {
      return null;
    }

    Div content = uiComponents.create(Div.class);
    content.setClassName("user-menu-header-content");

    String name = generateUserName(user);

    Avatar avatar = createAvatar(name);
    avatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

    Span text = uiComponents.create(Span.class);
    text.setText(name);
    text.setClassName("user-menu-text");

    content.add(avatar, text);

    if (name.equals(user.getUsername())) {
      text.addClassNames("user-menu-text-subtext");
    } else {
      Span subtext = uiComponents.create(Span.class);
      subtext.setText(user.getUsername());
      subtext.setClassName("user-menu-subtext");

      content.add(subtext);
    }

    return content;
  }

  private Avatar createAvatar(String fullName) {
    Avatar avatar = uiComponents.create(Avatar.class);
    avatar.setName(fullName);
    avatar.getElement().setAttribute("tabindex", "-1");
    avatar.setClassName("user-menu-avatar");

    return avatar;
  }

  private String generateUserName(User user) {
    String userName =
        String.format(
                "%s %s",
                Strings.nullToEmpty(user.getFirstName()), Strings.nullToEmpty(user.getLastName()))
            .trim();

    return userName.isEmpty() ? user.getUsername() : userName;
  }

  private boolean isSubstituted(User user) {
    UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
    return user != null && !authenticatedUser.getUsername().equals(user.getUsername());
  }
}
