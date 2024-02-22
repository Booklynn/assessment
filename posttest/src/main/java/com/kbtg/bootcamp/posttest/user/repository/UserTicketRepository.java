package com.kbtg.bootcamp.posttest.user.repository;

import com.kbtg.bootcamp.posttest.user.model.UserTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTicketRepository extends JpaRepository<UserTicket, Long> {
    List<UserTicket> findByUserUserId(String userId);
    List<UserTicket> findByUserUserIdAndLotteryTicket(String userId, String ticketId);
}
