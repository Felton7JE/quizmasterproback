package quizmaster.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.dto.RedeemRequest;
import quizmaster.quiz.dto.RedeemResponse;
import quizmaster.quiz.models.PromoCampaign;
import quizmaster.quiz.models.PromoCode;
import quizmaster.quiz.models.RedemptionLedger;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.PromoCodeRepository;
import quizmaster.quiz.repository.RedemptionLedgerRepository;
import quizmaster.quiz.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class PromoCodeService {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private RedemptionLedgerRepository ledgerRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public RedeemResponse redeem(Long userId, RedeemRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Código não pode estar vazio.");
        }

        String rawCode = request.getCode().trim();

        // 1. Verificação
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(rawCode)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou inexistente."));

        if (!promoCode.getIsActive()) {
            throw new IllegalArgumentException("Este código já não está ativo.");
        }

        PromoCampaign campaign = promoCode.getCampaign();
        if (!campaign.getIsActive()) {
            throw new IllegalArgumentException("A campanha promocional deste código já foi encerrada.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (campaign.getStartDate() != null && now.isBefore(campaign.getStartDate())) {
            throw new IllegalArgumentException("Esta promoção ainda não começou.");
        }
        if (campaign.getEndDate() != null && now.isAfter(campaign.getEndDate())) {
            throw new IllegalArgumentException("Esta promoção já expirou.");
        }

        // 2. Validação Contextual
        if (promoCode.getAssignedUser() != null && !promoCode.getAssignedUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Este código não pertence a esta conta.");
        }

        boolean alreadyRedeemed = ledgerRepository.existsByPromoCodeIdAndUserId(promoCode.getId(), userId);
        if (alreadyRedeemed) {
            throw new IllegalArgumentException("Você já resgatou este código anteriormente.");
        }

        if (campaign.getGlobalUsageLimit() != null) {
            long totalCampaignUses = ledgerRepository.countByPromoCode_CampaignId(campaign.getId());
            if (totalCampaignUses >= campaign.getGlobalUsageLimit()) {
                throw new IllegalArgumentException("O limite máximo de resgates para esta campanha já foi atingido.");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // 3. Resgate (Redemption)
        int newBalance = 0;
        if ("CRYSTALS".equalsIgnoreCase(campaign.getRewardType())) {
            int current = user.getCrystals() != null ? user.getCrystals() : 0;
            newBalance = current + campaign.getRewardAmount();
            user.setCrystals(newBalance);
        } else if ("COINS".equalsIgnoreCase(campaign.getRewardType())) {
            int current = user.getCoins() != null ? user.getCoins() : 0;
            newBalance = current + campaign.getRewardAmount();
            user.setCoins(newBalance);
        }
        // VIP_PASS could be added here in the future
        
        userRepository.save(user);

        // Marca código como inativo se for Single Use
        if (promoCode.getIsSingleUse()) {
            promoCode.setIsActive(false);
            promoCodeRepository.save(promoCode);
        }

        // Registra o resgate no Ledger
        RedemptionLedger ledger = new RedemptionLedger();
        ledger.setPromoCode(promoCode);
        ledger.setUser(user);
        ledger.setRewardType(campaign.getRewardType());
        ledger.setRewardGranted(campaign.getRewardAmount());
        ledgerRepository.save(ledger);

        return new RedeemResponse(
            true, 
            campaign.getRewardType(), 
            campaign.getRewardAmount(), 
            newBalance, 
            "Código resgatado com sucesso! +" + campaign.getRewardAmount() + " " + campaign.getRewardType()
        );
    }
}
