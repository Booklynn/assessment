package com.kbtg.bootcamp.posttest.user.service;

import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicket;
import com.kbtg.bootcamp.posttest.user.model.User;
import com.kbtg.bootcamp.posttest.user.model.UserTicket;
import com.kbtg.bootcamp.posttest.user.model.UserTicketResponse;
import com.kbtg.bootcamp.posttest.user.repository.UserTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserTicketService {
    private final UserTicketRepository userTicketRepository;

    @Autowired
    public UserTicketService(UserTicketRepository userTicketRepository) {
        this.userTicketRepository = userTicketRepository;
    }

    @Transactional
    public UserTicketResponse createUserTicket(User user, LotteryTicket lotteryTicket) {
        UserTicket userTicket = new UserTicket();
        userTicket.setUser(user);
        userTicket.setLottery(lotteryTicket);

        UserTicket savedUserTicket = userTicketRepository.save(userTicket);

        return new UserTicketResponse(savedUserTicket.getId());
    }

    public List<String> getUserLotteryTicketList(String userId) {
        List<UserTicket> userTickets = userTicketRepository.findByUserUserId(userId);
        return userTickets
                .stream()
                .map(userTicket -> userTicket.getLottery().getTicket())
                .toList();
    }

    public List<UserTicket> getUserLotteryTicketList(String userId, String ticketId) {
        List<UserTicket> userTickets = userTicketRepository.findByUserUserIdAndLotteryTicket(userId, ticketId);
        if (userTickets.isEmpty()) {
            throw new ResourceUnavailableException("userId: " + userId + " does not own this " + "ticketId: " + ticketId);
        }
        return userTickets;
    }

}
