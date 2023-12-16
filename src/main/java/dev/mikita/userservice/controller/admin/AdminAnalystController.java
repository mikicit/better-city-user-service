package dev.mikita.userservice.controller.admin;

import com.google.cloud.firestore.Query;
import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.moderator.CreateAnalystModeratorRequestDto;
import dev.mikita.userservice.dto.request.moderator.UpdateUserStatusModeratorRequestDto;
import dev.mikita.userservice.dto.response.moderator.AnalystModeratorResponseDto;
import dev.mikita.userservice.entity.Analyst;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.service.AnalystService;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/admin/analysts")
public class AdminAnalystController {
    private final AnalystService analystService;

    @Getter
    public enum OrderBy {
        CREATION_DATE("creationDate"),
        STATUS("status"),
        NAME("name");

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
    public AdminAnalystController(AnalystService analystService) {
        this.analystService = analystService;
    }

    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<Map<String, Object>> getAnalysts(
            @RequestParam(required = false) List<UserStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by", required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order
    ) {
        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;

        Pageable pageable = new Pageable(page, size, orderBy.getFieldName(), Query.Direction.valueOf(order.getFieldName()));
        PagedResult<Analyst> pageAnalysts = analystService.getAnalysts(statuses, pageable);
        List<Analyst> analysts = pageAnalysts.items();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("analysts", analysts.stream()
                .map(analyst -> modelMapper.map(analyst, AnalystModeratorResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageAnalysts.currentPage());
        response.put("totalItems", pageAnalysts.totalItems());
        response.put("totalPages", pageAnalysts.totalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<AnalystModeratorResponseDto> getAnalyst(@PathVariable String uid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        return ResponseEntity.ok(new ModelMapper().map(analystService.getAnalyst(uid), AnalystModeratorResponseDto.class));
    }

    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public void createAnalyst(@Valid @RequestBody CreateAnalystModeratorRequestDto requestDto)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        analystService.createAnalyst(new ModelMapper().map(requestDto, Analyst.class));
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<HttpStatus> updateAnalystStatus(@PathVariable String uid,
                                                          @RequestBody UpdateUserStatusModeratorRequestDto status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        analystService.updateAnalystStatus(uid, status.getStatus());
        return ResponseEntity.noContent().build();
    }
}
