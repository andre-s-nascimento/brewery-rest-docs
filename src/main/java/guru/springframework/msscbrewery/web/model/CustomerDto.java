package guru.springframework.msscbrewery.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Created by jt on 2019-04-21. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

  private UUID id;

  @NotBlank
  @Size(min = 3, max = 100)
  private String name;
}
