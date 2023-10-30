package guru.springframework.msscbrewery.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.BeerService;
import guru.springframework.msscbrewery.web.model.BeerDto;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs(uriHost = "dev.nascimento", uriScheme = "https", uriPort = 80)
@WebMvcTest(BeerController.class)
@ComponentScan(basePackages = "guru.springframework.msscbrewery.web.mappers")
class BeerControllerTest {

  @MockBean BeerService beerService;

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  BeerDto validBeer;

  @BeforeEach
  public void setUp(
      WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(documentationConfiguration(restDocumentation))
            .build();

    validBeer =
        BeerDto.builder()
            .id(UUID.randomUUID())
            .beerName("Beer1")
            .beerStyle("PALE_ALE")
            .upc(123456789012L)
            .build();
  }

  @Test
  void getBeer() throws Exception {
    given(beerService.getBeerById(any(UUID.class))).willReturn(validBeer);

    mockMvc
        .perform(
            get("/api/v1/beer/{beerId}", validBeer.getId().toString())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(validBeer.getId().toString()))
        .andExpect(jsonPath("$.beerName").value("Beer1"))
        .andDo(
            document(
                "v1/beer-get",
                pathParameters(parameterWithName("beerId").description("UUID of beer to get")),
                responseFields(
                    fieldWithPath("id").description("Id of Beer"),
                    fieldWithPath("beerName").description("Name of the Beer"),
                    fieldWithPath("beerStyle").description("Beer Style"),
                    fieldWithPath("upc").description("UPC of Beer"),
                    fieldWithPath("createdDate").description("Date Created"),
                    fieldWithPath("lastUpdatedDate").description("Date Updated"))));
  }

  @Test
  void handlePost() throws Exception {
    // given
    BeerDto beerDto = validBeer;
    beerDto.setId(null);
    BeerDto savedDto = BeerDto.builder().id(UUID.randomUUID()).beerName("New Beer").build();
    String beerDtoJson = objectMapper.writeValueAsString(beerDto);
    ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

    given(beerService.saveNewBeer(any())).willReturn(savedDto);

    mockMvc
        .perform(post("/api/v1/beer").contentType(MediaType.APPLICATION_JSON).content(beerDtoJson))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "v1/beer-new",
                requestFields(
                    fields.withPath("id").ignored(),
                    fields.withPath("beerName").description("Name of Beer to save"),
                    fields.withPath("beerStyle").description("Style of the beer"),
                    fields.withPath("upc").description("Beer UPC"),
                    fields.withPath("createdDate").ignored(),
                    fields.withPath("lastUpdatedDate").ignored())));
  }

  @Test
  void handleUpdate() throws Exception {
    // given
    BeerDto beerDto = validBeer;
    beerDto.setId(null);
    String beerDtoJson = objectMapper.writeValueAsString(beerDto);

    // when
    mockMvc
        .perform(
            put("/api/v1/beer/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
        .andExpect(status().isNoContent());

    then(beerService).should().updateBeer(any(), any());
  }

  private static class ConstrainedFields {
    private final ConstraintDescriptions constraintDescriptions;

    public ConstrainedFields(Class<?> input) {
      this.constraintDescriptions = new ConstraintDescriptions(input);
    }

    private FieldDescriptor withPath(String path) {
      return fieldWithPath(path)
          .attributes(
              key("constraints")
                  .value(
                      StringUtils.collectionToDelimitedString(
                          this.constraintDescriptions.descriptionsForProperty(path), ". ")));
    }
  }
}
