package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.converter.ArgumentConverter;
import ru.gafarov.betservice.entity.Argument;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.model.BetRole;
import ru.gafarov.betservice.repository.ArgumentRepository;
import ru.gafarov.betservice.service.ArgumentService;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.utils.CryptoUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class ArgumentServiceImpl implements ArgumentService {

    private final ArgumentRepository argumentRepository;
    private final BetService betService;
    private final MessageWithKeyService messageWithKeyService;

    /**
     * Аргумент в споре будет сохранен в БД в зашифрованном виде при условии, что хотя бы у одного из оппонентов включено шифрование в данный момент
     */
    @Override
    public ProtoBet.ResponseMessage save(ProtoBet.Argument protoArgument) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Bet bet = betService.getBet(protoArgument.getAuthor().getId(), protoArgument.getAuthor().getDialogStatus().getBetId());
        Argument argument = ArgumentConverter.toArgument(protoArgument, bet);
        /* Аргумент в споре будет сохранен в БД при условии, что хотя бы у одного из оппонентов включено шифрование в данный момент*/
        if (bet.getInitiator().isEncryptionEnabled() || bet.getOpponent().isEncryptionEnabled()) {
            User author;
            User receiver;
            if (argument.getBetRole().equals(BetRole.INITIATOR)) {
                author = bet.getInitiator();
                receiver = bet.getOpponent();
            } else {
                author = bet.getOpponent();
                receiver = bet.getInitiator();
            }
            argument.setText(CryptoUtils.encryptText(argument.getText(), messageWithKeyService.getPairSecret(author, receiver)));
            argument.setEncrypted(true);
        }
        Argument result = argumentRepository.save(argument);

        if (result.getId() != 0) {
            return betService.showBet(protoArgument.getAuthor().getId(), bet.getId());
        } else {
            return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }
}
