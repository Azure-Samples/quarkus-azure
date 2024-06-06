package no.kantega;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static java.util.Objects.requireNonNull;

@Path("/profile")
@Authenticated
public class ProfilePage {

    private final Template profile;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken accessToken;

    public ProfilePage(Template profile) {
        this.profile = requireNonNull(profile, "profile page is required");
    }

    @Path("/admin")
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("admin")
    public TemplateInstance getAdmin() {
        return getProfile();
    }

    @Path("/user")
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"user","admin"})
    public TemplateInstance getUser() {
        return getProfile();
    }

    private TemplateInstance getProfile() {
        return profile
                .data("identity", identity)
                .data("accessToken", accessToken);
    }

}
