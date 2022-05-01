package fr.adrienbrault.idea.symfony2plugin.config;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PhpNamespaceReference extends PsiPolyVariantReferenceBase<PsiElement> {

    private static final String NAMESPACE_SEPARATOR = "\\";

    @NotNull
    private final String namespaceFQN;

    public PhpNamespaceReference(@NotNull PsiElement psiElement, @NotNull String namespaceFQN) {
        super(psiElement);
        this.namespaceFQN = normalizeFQN(namespaceFQN);
    }

    public PhpNamespaceReference(@NotNull PsiElement psiElement, @NotNull String namespaceFQN, @NotNull TextRange textRange) {
        super(psiElement, textRange);
        this.namespaceFQN = normalizeFQN(namespaceFQN);
    }

    private String normalizeFQN(String fqn) {
        if (!fqn.startsWith(NAMESPACE_SEPARATOR)) {
            fqn = NAMESPACE_SEPARATOR + fqn;
        }

        if (fqn.endsWith(NAMESPACE_SEPARATOR)) {
            fqn = fqn.substring(0, fqn.length() - 1);
        }

        return fqn;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        var phpIndex = PhpIndex.getInstance(getElement().getProject());

        var results = new ArrayList<ResolveResult>();
        var namespaces = phpIndex.getNamespacesByName(namespaceFQN);
        for (var namespace : namespaces) {
            results.add(new PsiElementResolveResult(namespace));
        }

        return results.toArray(ResolveResult.EMPTY_ARRAY);
    }
}
