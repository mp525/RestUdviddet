package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.PersonDTO;
import dto.PersonsDTO;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import utils.EMF_Creator;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//Todo Remove or change relevant parts before ACTUAL use
@Path("person")
public class PersonResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();

    //An alternative way to get the EntityManagerFactory, whithout having to type the details all over the code
    //EMF = EMF_Creator.createEntityManagerFactory(DbSelector.DEV, Strategy.CREATE);
    private static final PersonFacade FACADE = PersonFacade.getFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String demo() {
        return "{\"msg\":\"Hello World\"}";
    }

    @Path("{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getPersonById(@PathParam("id") int id) throws PersonNotFoundException {
        PersonDTO person = FACADE.getPerson(id);
        return GSON.toJson(person);
    }

    @Path("all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllPersons() {
        PersonsDTO persons = FACADE.getAllPersons();
        return GSON.toJson(persons);
    }

    @POST
    @Consumes((MediaType.APPLICATION_JSON))
    @Produces((MediaType.APPLICATION_JSON))
    public Response addPerson(String person) throws MissingInputException {
        PersonDTO personDTO = GSON.fromJson(person, PersonDTO.class);
        PersonDTO newPersonDTO = FACADE.addPerson(personDTO.getFirstName(), personDTO.getLastName(), personDTO.getPhone(), personDTO.getStreet(), personDTO.getZip(), personDTO.getCity());
        return Response.ok(newPersonDTO).build();
    }

    @Path("{id}")
    @PUT
    @Consumes((MediaType.APPLICATION_JSON))
    @Produces((MediaType.APPLICATION_JSON))
    public Response editPerson(@PathParam("id") int id, String person) throws PersonNotFoundException, MissingInputException {
        PersonDTO personDTO = GSON.fromJson(person, PersonDTO.class);
        personDTO.setId(id);
        PersonDTO newPersonDTO = FACADE.editPerson(personDTO);
        return Response.ok(newPersonDTO).build();
    }
    
    @Path("{id}")
    @DELETE
    @Produces((MediaType.APPLICATION_JSON))
    public String deletePerson(@PathParam("id") int id) throws PersonNotFoundException{
        PersonDTO deleted = FACADE.deletePerson(id);
        return "{\"status of " + deleted.getFirstName() + deleted.getLastName() + "\":\"removed\"}";
    }
}
