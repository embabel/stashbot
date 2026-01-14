package com.embabel.stashbot.vaadin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;

/**
 * Vaadin app shell configuration with Push enabled for async UI updates.
 * Using LONG_POLLING transport for broad compatibility.
 */
@Push(value = PushMode.AUTOMATIC, transport = Transport.LONG_POLLING)
@Theme("stashbot")
public class AppShellConfig implements AppShellConfigurator {
}
