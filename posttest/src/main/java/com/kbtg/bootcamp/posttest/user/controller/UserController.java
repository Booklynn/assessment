package com.kbtg.bootcamp.posttest.user.controller;

import com.kbtg.bootcamp.posttest.exception.InternalServiceException;
import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicketResponse;
import com.kbtg.bootcamp.posttest.user.model.UserTicketListResponse;
import com.kbtg.bootcamp.posttest.user.model.UserTicketResponse;
import com.kbtg.bootcamp.posttest.user.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/lotteries/{ticketId}")
    public ResponseEntity<UserTicketResponse> AddUserLotteryTicket(
            @PathVariable("userId") @NotBlank @Size(min = 10, max = 10) String userId,
            @PathVariable("ticketId") @NotBlank @Size(min = 6, max = 6) String ticketId
    ) {
        try {
            UserTicketResponse response = userService.purchaseLotteryTicket(userId, ticketId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServiceException("An internal error occurred when purchasing a lottery ticket to user");
        }
    }

    @GetMapping("/{userId}/lotteries")
    public ResponseEntity<UserTicketListResponse> getUserLotteryTickets(
            @PathVariable("userId") @NotBlank @Size(min = 10, max = 10) String userId
    ) {
        try {
            UserTicketListResponse response = userService.getUserLotteryTicketList(userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServiceException("An internal error occurred when getting lottery ticket list");
        }
    }

    @DeleteMapping("/{userId}/lotteries/{ticketId}")
    public ResponseEntity<LotteryTicketResponse> deleteUserLotteryTickets(
            @PathVariable("userId") @NotBlank @Size(min = 10, max = 10) String userId,
            @PathVariable("ticketId") @NotBlank @Size(min = 6, max = 6) String ticketId
    ) {
        try {
            LotteryTicketResponse response = userService.sellLotteryTickets(userId, ticketId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServiceException("An internal error occurred when selling lottery tickets");
        }
    }

}
