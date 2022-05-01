package fr.adrienbrault.idea.symfony2plugin.tests.config.yaml;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpDefine;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import fr.adrienbrault.idea.symfony2plugin.tests.SymfonyLightCodeInsightFixtureTestCase;
import org.jetbrains.yaml.YAMLFileType;

public class YamlReferenceContributorTest extends SymfonyLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("YamlReferenceContributor.php");
    }

    public String getTestDataPath() {
        return "src/test/java/fr/adrienbrault/idea/symfony2plugin/tests/config/yaml/fixtures";
    }

    public void testConstantProvidesReferences() {
        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
                "  app.service.example:\n" +
                "    arguments:\n" +
                "      - !php/const CONST_<caret>FOO\n",
            PlatformPatterns.psiElement(PhpDefine.class).withName("CONST_FOO")
        );

        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
            "  app.service.example:\n" +
            "    arguments:\n" +
            "      - !php/const Foo\\Bar::F<caret>OO\n",
            PlatformPatterns.psiElement(Field.class).withName("FOO")
        );
    }

    public void testClassFQNProvidesReferences() {
        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
            "  app.service.example:\n" +
            "    class: Foo\\B<caret>ar\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
            "  app.service.example:\n" +
            "    class: Foo<caret>\\Bar\n",
            PlatformPatterns.psiElement(PhpNamespace.class).withName("Foo")
        );

        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
            "  Foo\\Bar<caret>: ~\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
            "  _instanceof\n"+
            "    Foo<caret>\\Bar: \n" +
            "      public: true",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertReferenceMatchOnParent(
            YAMLFileType.YML,
            "services:\n" +
            "  _instanceof\n"+
            "    Foo<caret>\\Bar: \n" +
            "      public: true",
            PlatformPatterns.psiElement(PhpNamespace.class).withName("Foo")
        );
    }
}
