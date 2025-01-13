package client.utils;

import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class LocaleUtil {

    public static final String BUNDLE_BASE_NAME = "locale/translations";
    private List<Locale> availableLocales;

    public List<Locale> getAvailableLocales() {
        // Lazy-loading
        if (availableLocales == null) {
            Set<ResourceBundle> resourceBundles = new HashSet<>();
            for (Locale locale : Locale.getAvailableLocales()) {
                try {
                    resourceBundles.add(ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale));
                } catch (MissingResourceException _) {}
            }
            this.availableLocales = resourceBundles.stream()
                    .map(ResourceBundle::getLocale)
                    .filter(locale -> !locale.toLanguageTag().equals("und"))
                    .toList();
            System.out.println("Found " + availableLocales.size() + " available locales: "
                    + availableLocales.stream().map(Locale::toString).collect(Collectors.joining(", ")));
        }

        return availableLocales;
    }

    public Image getFlagImage(Locale locale) {
        try {
            return new Image("flags/" + locale.toLanguageTag() + ".png");
        } catch (IllegalArgumentException ex) {
            System.err.println("Loading flag image failed for " + locale);
            return null;
        }
    }

}
