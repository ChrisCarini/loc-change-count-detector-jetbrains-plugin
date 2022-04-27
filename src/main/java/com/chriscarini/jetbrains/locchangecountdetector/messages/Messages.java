package com.chriscarini.jetbrains.locchangecountdetector.messages;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;


/**
 * Localized messages for LoC COP.
 */
public final class Messages {
  @NonNls
  private static final String BUNDLE = "messages.loc_cop";
  private static Reference<ResourceBundle> bundle;

  private Messages() {
  }

  public static String message(@NotNull @NonNls @PropertyKey(resourceBundle = BUNDLE) final String key, @NotNull final Object... params) {
    return AbstractBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(Messages.bundle);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      Messages.bundle = new SoftReference<>(bundle);
    }
    return bundle;
  }
}
