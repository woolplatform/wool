package eu.woolplatform.utils.i18n;

/**
 * This class can return a string in the current language for a given resource
 * ID.
 *
 * @author Dennis Hofs (RRD)
 */
public interface I18nStringResolver {

	/**
	 * Returns the string for the specified resource ID.
	 *
	 * @param resourceId the resource ID
	 * @return the string
	 */
	CharSequence resolve(String resourceId);
}
