package com.project.baedalsodae.user.dto.request;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    private String username;
    private String name;
    private Boolean isDeleted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
