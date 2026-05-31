package com.insurance.security.view.user;

import com.insurance.security.entity.User;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "users", layout = DefaultMainViewParent.class)
@ViewController(id = "app_User.list")
@ViewDescriptor(path = "user-list-view.xml")
@LookupComponent("usersDataGrid")
@DialogMode(width = "64em")
public class UserListView extends StandardListView<User> {
}
