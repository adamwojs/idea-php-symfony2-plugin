package fr.adrienbrault.idea.symfony2plugin.config;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import fr.adrienbrault.idea.symfony2plugin.stubs.ServiceIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ServiceReference extends PsiPolyVariantReferenceBase<PsiElement> {

    @NotNull
    private final String serviceName;

    public ServiceReference(@NotNull PsiElement psiElement, @NotNull String serviceName) {
        super(psiElement);

        this.serviceName = normalizeServiceName(serviceName);
    }

    @NotNull
    private String normalizeServiceName(@NotNull String serviceName) {
        if (serviceName.startsWith("@")) {
            // Remove '@' prefix
            serviceName = serviceName.substring(1);
        }
        return serviceName;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        var results = new ArrayList<ResolveResult>();

        var definitions = ServiceIndexUtil.findServiceDefinitions(getElement().getProject(), serviceName);
        for (var definition : definitions) {
            results.add(new PsiElementResolveResult(definition));
        }

        return results.toArray(ResolveResult.EMPTY_ARRAY);
    }
}
