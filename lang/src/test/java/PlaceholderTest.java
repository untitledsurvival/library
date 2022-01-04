import com.untitledsurvival.lib.lang.placeholder.Placeholder;
import com.untitledsurvival.lib.lang.placeholder.PlaceholderAPI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.untitledsurvival.lib.lang.placeholder.PlaceholderAPI.apply;

public class PlaceholderTest {
    @Before
    public void setup() {
        PlaceholderAPI.getNamespacePlaceholders().clear();

        PlaceholderAPI.register(new DummyPH());
        PlaceholderAPI.register(new Dummy2PH());
        PlaceholderAPI.register((Placeholder.Wildcard) (object, placeholderName) -> switch (placeholderName) {
            case "present1" -> "demo1";
            case "present2" -> "demo2";
            case "present3" -> "demo3";

            default -> null;
        });

        // this contains Spigot named classes that cannot be referenced in a unit test
        PlaceholderAPI.getNamespacePlaceholders().remove("event");
    }

    @Test
    public void genericsTest() {
        Dummy dummy = new Dummy("HEY");
        OtherDummy dummy2 = new OtherDummy("hello");

        String message = "This dummy says %dummy:name%";

        Assert.assertEquals(apply(message, dummy), "This dummy says HEY");
        Assert.assertEquals(apply(message, dummy2), message);
        Assert.assertEquals(apply("%dummy2:name%", dummy2), dummy2.name);

        Assert.assertEquals(apply("%ambig%"), "%ambig%");
    }

    @Test
    public void nullTest() {
        Assert.assertEquals(apply("%no_case%"), "%no_case%");
        Assert.assertEquals(apply("%present1%"), "demo1");
        Assert.assertEquals(apply("%present1% %present3%"), "demo1 demo3");
    }

    @Test
    public void register() {
        Assert.assertThrows(IllegalArgumentException.class, () -> PlaceholderAPI.register(new DummyPH()));
    }

    public static class DummyPH implements Placeholder<Dummy> {
        @Override
        public String apply(Dummy dummy, String placeholderName) {
            return switch (placeholderName) {
                case "name" -> dummy.name;
                default -> null;
            };
        }

        @Override
        public String getNamespace() {
            return "dummy";
        }

        @Override
        public Class<Dummy> getType() {
            return Dummy.class;
        }
    }

    public static class Dummy2PH implements Placeholder<OtherDummy> {
        @Override
        public String apply(OtherDummy dummy, String placeholderName) {
            return switch (placeholderName) {
                case "name" -> dummy.name;
                default -> null;
            };
        }

        @Override
        public String getNamespace() {
            return "dummy2";
        }

        @Override
        public Class<OtherDummy> getType() {
            return OtherDummy.class;
        }
    }

    public record Dummy(String name) { }
    public record OtherDummy(String name) { }
}
