package com.jeonsoft.facebundypro.utils.ocpsoft;

import java.util.Locale;

/**
 * An object that behaves differently for various {@link java.util.Locale} settings.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface LocaleAware<TYPE>
{
    /**
     * Set the {@link java.util.Locale} for which this instance should behave in.
     */
    public TYPE setLocale(Locale locale);

}