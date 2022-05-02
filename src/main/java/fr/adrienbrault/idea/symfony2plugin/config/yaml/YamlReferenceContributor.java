package fr.adrienbrault.idea.symfony2plugin.config.yaml;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.config.PhpClassReference;
import fr.adrienbrault.idea.symfony2plugin.config.PhpNamespaceReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.*;

public class YamlReferenceContributor extends PsiReferenceContributor {
    private static final String TAG_PHP_CONST = "!php/const";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLScalar.class)
                .withText(StandardPatterns.string()
                    .contains(TAG_PHP_CONST)
                ),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    if (!Symfony2ProjectComponent.isEnabled(element)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    var scalar = (YAMLScalar) element;
                    if (scalar.getTextValue().isEmpty()) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    return new PsiReference[]{
                        new ConstantYamlReference(scalar)
                    };
                }
            }
        );

        // app.service.example
        //     class: <App\Service\Example>
        registrar.registerReferenceProvider(
            PlatformPatterns
                .psiElement(YAMLScalar.class)
                .withParent(PlatformPatterns
                    .psiElement(YAMLKeyValue.class)
                    .withName(
                        PlatformPatterns.string().oneOf("class")
                    )
                ),
            new ClassFQNPsiReferenceProvider() {
                @Override
                protected @Nullable String getClassFQNFromPsiElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    if (element instanceof YAMLScalar) {
                        return ((YAMLScalar) element).getTextValue();
                    }

                    return null;
                }
            }
        );

        // services:
        //     My<caret>Class: ~
        registrar.registerReferenceProvider(
            PlatformPatterns
                .psiElement(YAMLKeyValue.class)
                .withParent(PlatformPatterns
                    .psiElement(YAMLMapping.class)
                    .withParent(PlatformPatterns
                        .psiElement(YAMLKeyValue.class)
                        .withName("services", "_instanceof"))
                ),
            new ClassFQNPsiReferenceProvider() {
                @Override
                protected @Nullable String getClassFQNFromPsiElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    if (element instanceof YAMLKeyValue) {
                        var serviceId = ((YAMLKeyValue) element).getKeyText();
                        if (serviceId.contains(".") || serviceId.endsWith(NAMESPACE_SEGMENT_SEPARATOR)) {
                            // Skip old fashion service ids
                            return null;
                        }

                        return serviceId;
                    }

                    return null;
                }
            }
        );
    }

    private static abstract class ClassFQNPsiReferenceProvider extends PsiReferenceProvider {

        protected static final String NAMESPACE_SEGMENT_SEPARATOR = "\\";

        @Override
        public final PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            if (!Symfony2ProjectComponent.isEnabled(element)) {
                return PsiReference.EMPTY_ARRAY;
            }

            var classFQN = getClassFQNFromPsiElement(element, context);
            if (classFQN == null || classFQN.isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }

            return doGetReferencesByElement(element, classFQN);
        }

        @Nullable
        protected abstract String getClassFQNFromPsiElement(
            @NotNull PsiElement element,
            @NotNull ProcessingContext context
        );

        private PsiReference[] doGetReferencesByElement(@NotNull PsiElement element, @NotNull String classFQN) {
            var references = new ArrayList<PsiReference>();

            int idx = -1, prev = -1;
            while ((idx = classFQN.indexOf(NAMESPACE_SEGMENT_SEPARATOR, idx + 1)) != -1) {
                var namespace = classFQN.substring(0, idx);
                if (!namespace.isEmpty()) {
                    references.add(new PhpNamespaceReference(
                        element,
                        namespace,
                        new TextRange(prev + 1, namespace.length()))
                    );
                }

                prev = idx;
            }

            references.add(new PhpClassReference(
                element,
                classFQN,
                new TextRange(classFQN.lastIndexOf(NAMESPACE_SEGMENT_SEPARATOR) + 1, classFQN.length())
            ));

            return references.toArray(new PsiReference[0]);
        }
    }
}
