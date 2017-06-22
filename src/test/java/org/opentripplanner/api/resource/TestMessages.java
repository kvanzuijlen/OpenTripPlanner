/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.api.resource;

import java.util.Locale;

import org.opentripplanner.api.common.Message;

import junit.framework.TestCase;

public class TestMessages extends TestCase {

    public void testLanguagesMultipleGetMethods() {

        // Force default to make test work on non-US machines
        Locale.setDefault(new Locale("en", "US"));

        String english = Message.GEOCODE_FROM_AMBIGUOUS.get();
        String french = Message.GEOCODE_FROM_AMBIGUOUS.get(Locale.CANADA_FRENCH);
        String spanish = Message.GEOCODE_FROM_AMBIGUOUS.get(new Locale("es"));

        TestCase.assertNotNull(english);
        TestCase.assertNotNull(french);
        TestCase.assertNotNull(spanish);
        TestCase.assertNotSame(english, french);
        TestCase.assertNotSame(english, spanish);
        TestCase.assertNotSame(french, spanish);
    }

    public void testDefaultLanguage() {
        Locale.setDefault(new Locale("en", "US"));

        String english = Message.GEOCODE_FROM_AMBIGUOUS.get();

        TestCase.assertNotNull(english);
    }

    public void testGetLanguageWithEnum() {
        Locale.setDefault(new Locale("en", "US"));

        String english = Message.GEOCODE_FROM_AMBIGUOUS.get(Locale.ENGLISH);
        String french = Message.GEOCODE_FROM_AMBIGUOUS.get(Locale.CANADA_FRENCH);

        TestCase.assertNotNull(english);
        TestCase.assertNotNull(french);
        TestCase.assertNotSame("Test errored, English shouldn't be the same as French", english, french);
    }

    public void testGetLanguageFromNewLocale() {
        Locale.setDefault(new Locale("en", "US"));

        String english = Message.GEOCODE_FROM_AMBIGUOUS.get(new Locale("en"));
        String french = Message.GEOCODE_FROM_AMBIGUOUS.get(new Locale("fr"));
        String spanish = Message.GEOCODE_FROM_AMBIGUOUS.get(new Locale("es"));

        TestCase.assertNotNull(english);
        TestCase.assertNotNull(french);
        TestCase.assertNotNull(spanish);
        TestCase.assertNotSame("Test errored, English shouldn't be the same as French", english, french);
        TestCase.assertNotSame("Test errored, English shouldn't be the same as Spanish", english, spanish);
        TestCase.assertNotSame("Test errored, French shouldn't be the same as Spanish", french, spanish);
    }
}
