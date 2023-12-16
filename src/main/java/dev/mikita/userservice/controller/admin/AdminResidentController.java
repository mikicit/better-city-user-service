package dev.mikita.userservice.controller.admin;

import com.google.cloud.firestore.Query;
import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.moderator.UpdateUserStatusModeratorRequestDto;
import dev.mikita.userservice.dto.response.moderator.ResidentModeratorResponseDto;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.service.ResidentService;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/residents")
public class AdminResidentController {
    private final ResidentService residentService;

    @Getter
    public enum OrderBy {
        CREATION_DATE("creationDate"),
        STATUS("status"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName");

        private final String fieldName;

        OrderBy(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    @Getter
    public enum Order {
        ASC("ASCENDING"),
        DESC("DESCENDING");

        private final String fieldName;

        Order(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    @Autowired
    public AdminResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<Map<String, Object>> getResidents(
            @RequestParam(required = false) List<UserStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by", required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order) {
        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;

        Pageable pageable = new Pageable(page, size, orderBy.getFieldName(), Query.Direction.valueOf(order.getFieldName()));
        PagedResult<Resident> pageResidents = residentService.getResidents(statuses, pageable);
        List<Resident> residents = pageResidents.items();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("residents", residents.stream()
                .map(resident -> modelMapper.map(resident, ResidentModeratorResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageResidents.currentPage());
        response.put("totalItems", pageResidents.totalItems());
        response.put("totalPages", pageResidents.totalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<ResidentModeratorResponseDto> getResident(@PathVariable String uid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        return ResponseEntity.ok(new ModelMapper().map(residentService.getResident(uid), ResidentModeratorResponseDto.class));
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<HttpStatus> updateResidentStatus(@PathVariable String uid,
                                                          @RequestBody UpdateUserStatusModeratorRequestDto status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        residentService.updateResidentStatus(uid, status.getStatus());
        return ResponseEntity.noContent().build();
    }
}
