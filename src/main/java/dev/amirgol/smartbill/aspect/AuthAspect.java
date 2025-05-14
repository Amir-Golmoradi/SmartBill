package dev.amirgol.smartbill.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Aspect
@Component
public class AuthAspect {


    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) throws AccessDeniedException {
        String neededRole = requireRole.value();
        String currentRole = "ROLE_ADMIN"; // => In the future, must be replaced with Spring SecurityContext, which resolves the user's role dynamically

        if (!neededRole.equals(currentRole)) {
            throw new AccessDeniedException(
                    "🚫 Access denied: requires role " + neededRole + " but was " + currentRole
            );
        }
    }

}
