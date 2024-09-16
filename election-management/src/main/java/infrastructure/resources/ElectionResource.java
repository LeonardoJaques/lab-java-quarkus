package infrastructure.resources;

import api.dto.out.Election;
import api.ElectionApi;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("api/elections")
public class ElectionResource {
	private final ElectionApi api;
	
	public ElectionResource(ElectionApi api) {
		this.api = api;
	}
	
	
	@POST
	@ResponseStatus(RestResponse.StatusCode.CREATED)
	@Transactional
	public void submit(){
			api.submit();
	}

	@GET
	public List<Election> list() {
		return api.findAll();
	}


}
