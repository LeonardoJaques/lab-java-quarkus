package infrastructure.resources;

import api.ElectionApi;
import api.dto.out.Election;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Path("api/voting")
public class VotingResource {
    private final ElectionApi api;

    public VotingResource(ElectionApi api) {
        this.api = api;
    }

    @GET
    public List<Election> findAll() {
        return api.findAll();
    }

    @POST
    @Path("elections/{electionId}/candidates/{candidateId}")
    @ResponseStatus(RestResponse.StatusCode.ACCEPTED)
    @Transactional
    public void vote(@PathParam("electionId") String electionId,
            @PathParam("candidateId") String candidateId) {
        if (electionId == null || electionId.isBlank()) {
            throw new BadRequestException("Election ID cannot be blank");
        }
        if (candidateId == null || candidateId.isBlank()) {
            throw new BadRequestException("Candidate ID cannot be blank");
        }
        api.vote(electionId, candidateId);
    }
}
