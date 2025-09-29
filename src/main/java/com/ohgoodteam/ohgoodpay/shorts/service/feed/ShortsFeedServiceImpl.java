package com.ohgoodteam.ohgoodpay.shorts.service.feed;

import com.ohgoodteam.ohgoodpay.common.entity.CommentEntity;
import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.ReactionEntity;
import com.ohgoodteam.ohgoodpay.common.entity.ShortsEntity;
import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
// import com.ohgoodteam.ohgoodpay.shorts.Converter;
import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsCommentRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsPointEarnRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.feed.ShortsCommentDataDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.feed.ShortsFeedDataDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsReactionRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.feed.*;
import com.ohgoodteam.ohgoodpay.shorts.enums.profile.SubscriptionStatus;
import com.ohgoodteam.ohgoodpay.shorts.repository.CommentRepository;
import com.ohgoodteam.ohgoodpay.shorts.repository.ReactionRepository;
import com.ohgoodteam.ohgoodpay.shorts.repository.ShortsRepository;
import com.ohgoodteam.ohgoodpay.shorts.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortsFeedServiceImpl implements ShortsFeedService {

    private final JWTUtil jwtUtil;

    private final CustomerRepository customerRepository;
    private final ShortsRepository shortsRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${ranking.w.like:1.5}")    private double wLike;
    @Value("${ranking.w.comment:1.2}") private double wComment;
    @Value("${ranking.w.hashtag:1.2}") private double wHashtag;
    @Value("${ranking.w.recency:1.5}") private double wRecency;
    @Value("${ranking.tau.hours:72}")  private double tauHours;

    private static final int POINTS_PER_60_SECONDS = 10;
    private static final int DAILY_POINT_LIMIT = 100;
    private static final String POINT_REASON = "숏폼 시청";

    // private final Converter converter;

    /**
     * 전체 쇼츠 피드 조회 (기존 Pageable 방식)
     * @param page
     * @param size
     * @param keyword
     * @return
     */
    @Override
    public List<ShortsFeedDataDTO> findAllFeeds(int page, int size, String keyword, Long customerId) {
        log.info("findAllFeeds 호출 : page={}, size={}, keyword={}, customerId={}", page, size, keyword, customerId);

        Page<Map<String, Object>> results;
        Pageable pageable = PageRequest.of(page - 1, size);
        if(customerId < 1) {
            results = shortsRepository.findAllFeedsNoToken(wLike, wComment, wHashtag, wRecency, tauHours, pageable);
        } else {
            results = shortsRepository.findAllFeeds(wLike, wComment, wHashtag, wRecency, tauHours, customerId, pageable);
        }

        // Object[] 배열을 ShortsFeedDataDto로 변환
        List<ShortsFeedDataDTO> result = new ArrayList<>();
        for (Map<String, Object> row : results.getContent()) {
            result.add(convertToShortsFeedDataDto(row,customerId));
        }

        log.info("JPA 조회 결과: {}", result);
        log.info("페이징 정보 - 현재페이지: {}, 전체페이지: {}, 다음페이지존재: {}", 
                results.getNumber() + 1, results.getTotalPages(), results.hasNext());

        return result;
    }


    /**
     * Object[] 배열을 ShortsFeedDataDto로 변환
     */
    private ShortsFeedDataDTO convertToShortsFeedDataDto(Map<String, Object> row, Long targetId) {
        // 각 컬럼을 의미있는 변수명으로 추출
        Long shortsId = (Long) row.get("shortsId");
        String videoName = (String) row.get("videoName");
        String thumbnail = (String) row.get("thumbnail");
        String shortsName = (String) row.get("shortsName");
        String shortsExplain = (String) row.get("shortsExplain");
        LocalDateTime date = ((Timestamp) row.get("date")).toLocalDateTime();
        Long customerId = Long.parseLong(String.valueOf(row.get("customerId")));
        String customerNickname = (String) row.get("nickname");
        String profileImg = (String) row.get("profileImg");
        int likeCount = ((Number) row.get("likeCount")).intValue();
        int commentCount = ((Number) row.get("commentCount")).intValue();
        String myReaction = (String) row.get("myReaction"); // myReaction는 11번째 인덱스
        Double score = ((Number) row.get("score")).doubleValue(); // score는 12번째 인덱스

        if(targetId == 0) {
            myReaction = null; // 비로그인 사용자는 myReaction 무조건 null
        }

        SubscriptionStatus subscriptionStatus;
        if(targetId.equals(customerId)){
            subscriptionStatus = SubscriptionStatus.SELF;
        } else if(subscriptionRepository.existsByFollowerCustomerIdAndFollowingCustomerId(targetId, customerId)){
            subscriptionStatus = SubscriptionStatus.SUBSCRIBED;
        } else {
            subscriptionStatus = SubscriptionStatus.NOT_SUBSCRIBED;
        }
        
        // 점수 콘솔 출력
        log.info("쇼츠 ID: {}, 제목: {}, 좋아요: {}, 댓글: {}, 점수: {}", 
                shortsId, shortsName, likeCount, commentCount, String.format("%.6f", score));
        
        return ShortsFeedDataDTO.builder()
                .shortsId(shortsId)
                .videoName(videoName)
                .thumbnail(thumbnail)
                .shortsName(shortsName)
                .shortsExplain(shortsExplain)
                .date(date)
                .customerId(customerId)
                .customerNickname(customerNickname)
                .profileImg(profileImg)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .myReaction(myReaction)
                .subscriptionStatus(subscriptionStatus)
                .build();
    }

    /**
     * 댓글 조회
     * @param shortsId
     * @return
     */
    @Override
    public List<ShortsCommentDataDTO> findAllComments(Long shortsId, Long customerId) {
        List<CommentEntity> result = commentRepository.findAllComments(shortsId);

        List<ShortsCommentDataDTO> data = new ArrayList<>();
        for(CommentEntity item : result){
            ShortsCommentDataDTO dto = new ShortsCommentDataDTO(item);
            if(!"0".equals(customerId) && item.getCustomer().getCustomerId().equals(customerId)){
                dto.setDeletable(true);
            } else {
                dto.setDeletable(false);
            }
            data.add(dto);
        }
        return data;
    }

    /**
     * 댓글 작성
     * @param shortsCommentRequestDto
     * @return
     */
    @Override
    @Transactional
    public boolean createComment(Long shortsId, ShortsCommentRequestDTO shortsCommentRequestDto, Long customerId) {
        // 1. 작성자 조회
        CustomerEntity customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 쇼츠 조회
       ShortsEntity shorts =  shortsRepository.findById(shortsId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쇼츠입니다."));

        // 3. 댓글 생성
        CommentEntity comment = CommentEntity.builder()
                .customer(customer)
                .shorts(shorts)
                .gno(shortsCommentRequestDto.getGno())
                .content(shortsCommentRequestDto.getContent())
                .date(LocalDateTime.now())
                .build();

        // 4. DB 저장
         commentRepository.save(comment);

        // 5. 댓글 작성 시 쇼츠의 댓글 수 증가

        int result = shortsRepository.incrementCommentCount(shortsId);


        // 6. 성공 여부 반환
        return result > 0;
    }

    /**
     * 좋아요/싫어요 반응처리
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ShortsReactionDataDTO reactToShorts(ShortsReactionRequestDTO dto, Long customerId) {
        // 1. 작성자 조회
        CustomerEntity customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 쇼츠 조회
        ShortsEntity shorts = shortsRepository.findById(dto.getShortsId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쇼츠입니다."));

        // 3. 요청 타입 (대소문자 무시 -> 항상 대문자로 저장)
//        String requestedType = dto.getType().toUpperCase();
        String requestedType = dto.getType();

        // 4. 기존 반응 조회
        Optional<ReactionEntity> existingReaction = reactionRepository.findByCustomerAndShorts(customer, shorts);

        // 5. 기존 반응이 존재하는 경우
        if(existingReaction.isPresent()){
            String currentType = existingReaction.get().getReact();

            if(currentType.equals(requestedType)){
                // 같은 반응 -> 취소 (토글)
                reactionRepository.delete(existingReaction.get());

                if("like".equals(requestedType)){
                    // 좋아요 취소 -> 좋아요 수 감소
                    shortsRepository.decrementLikeCount(dto.getShortsId());
                }
                // 싫어요 취소는 좋아요 수 변경 없음

                // 최종 좋아요 수 조회
//                ShortsEntity updatedShorts = shortsRepository.findById(dto.getShortsId()).orElseThrow();
//                int likeCount = (int) updatedShorts.getLikeCount();
                int likeCount = shortsRepository.findLikeCountById(dto.getShortsId());

                return ShortsReactionDataDTO.builder()
                        .shortsId(dto.getShortsId())
                        .likeCount(likeCount)
                        .myReaction(null)
                        .build();
            }
            else {
                // 다른 반응으로 전환
                reactionRepository.updateReaction(customer, shorts, requestedType);

                if("like".equals(currentType) && "dislike".equals(requestedType)){
                    // 좋아요 -> 싫어요: 좋아요 취소되므로 좋아요 수 감소
                    shortsRepository.decrementLikeCount(dto.getShortsId());
                } else if("dislike".equals(currentType) && "like".equals(requestedType)){
                    // 싫어요 -> 좋아요: 좋아요 저장되므로 좋아요 수 증가
                    shortsRepository.incrementLikeCount(dto.getShortsId());
                }

                int likeCount = shortsRepository.findLikeCountById(dto.getShortsId());

                return ShortsReactionDataDTO.builder()
                        .shortsId(dto.getShortsId())
                        .likeCount(likeCount)
                        .myReaction(requestedType)
                        .build();

            }
        }

        // 6. 새 반응 저장 (기존 반응이 없거나 다른 반응으로 전환하는 경우)

        ReactionEntity reaction = ReactionEntity.builder()
                .react(requestedType)
                .customer(customer)
                .shorts(shorts)
                .build();

        reactionRepository.save(reaction);

        if("like".equals(requestedType)){
            // 좋아요 저장 -> 좋아요 수 증가
            shortsRepository.incrementLikeCount(dto.getShortsId());
        }
        // 싫어요 저장은 좋아요 수 변경 없음

        // 7. 최종 좋아요 수 조회
//        ShortsEntity updatedShorts = shortsRepository.findById(dto.getShortsId()).orElseThrow();
//        int likeCount = (int) updatedShorts.getLikeCount();
        int likeCount = shortsRepository.findLikeCountById(dto.getShortsId());

        return ShortsReactionDataDTO.builder()
                .shortsId(dto.getShortsId())
                .likeCount(likeCount)
                .myReaction(requestedType)
                .build();
    }


    /**
     * 댓글 삭제
     * @param shortsId
     * @param commentId
     * @param customerId
     * @return
     */
    @Override
    public ShortsCommentDeleteDataDTO deleteComment(Long shortsId, Long commentId, Long customerId) {

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getShorts().getShortsId().equals(shortsId)) {
            throw new IllegalArgumentException("해당 피드에 속한 댓글이 아닙니다.");
        }

        if (!comment.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.deleteById(commentId);

        if (!commentRepository.existsById(commentId)){
            // 댓글 삭제 후 쇼츠의 댓글 수 감소
            shortsRepository.decrementCommentCount(shortsId);
        }

        return ShortsCommentDeleteDataDTO.builder()
                .commentId(commentId)
                .shortsId(shortsId)
                .customerId(customerId)
                .deleted(true)
                .build();
    }

    // 숏폼 피드 공유 기능 -> 특정 영상에 대한 정보 반환
    @Override
    public ShortsFeedDataDTO getSpecificShorts(Long shortsId) {
        ShortsEntity shorts = shortsRepository.findById(shortsId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쇼츠입니다."));
        return new ShortsFeedDataDTO(shorts);
    }

    // 포인트 게이지 적립
    @Override
    @Transactional
    public ShortsPointEarnResponseDTO earnPoint(Long customerId, ShortsPointEarnRequestDTO requestDto) {
        int watchedSeconds = requestDto.watchedSeconds();
        
        // 1. 오늘 적립된 포인트 합계 조회
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);
        
        int todayTotalPoints = shortsRepository.sumTodayPoints(
            customerId, POINT_REASON, startOfToday, startOfTomorrow);
        
        // 2. 적립 가능한 포인트 계산
        int calculatedPoints = (int) Math.round((double) watchedSeconds * POINTS_PER_60_SECONDS / 60);
        int remainingLimit = DAILY_POINT_LIMIT - todayTotalPoints;
        int earnedPoints = Math.min(calculatedPoints, remainingLimit);
        log.info("calculatedPoints", calculatedPoints);
        log.info("remainingLimit", remainingLimit);
        log.info("earnedPoints", earnedPoints);
        
        // 3. 포인트 적립 
        if (earnedPoints > 0) {
            // 포인트 히스토리 저장
            shortsRepository.insertPointHistory(
                customerId, earnedPoints, POINT_REASON, LocalDateTime.now());
            
            // 사용자 포인트 업데이트
            shortsRepository.updateCustomerPoints(customerId, earnedPoints);
            
            return new ShortsPointEarnResponseDTO(
                earnedPoints,
                todayTotalPoints + earnedPoints,
                true,
                earnedPoints + "포인트 적립 완료!"
            );
        } else {
            return new ShortsPointEarnResponseDTO(
                0,
                todayTotalPoints,
                false,
                remainingLimit == 0 ? "일일 포인트 한도 초과" : "적립 가능한 포인트 없음"
            );
        }
    }
}



