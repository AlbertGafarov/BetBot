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

    @Override
    public ProtoBet.ResponseMessage save(ProtoBet.Argument protoArgument) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Bet bet = betService.getBet(protoArgument.getAuthor().getId(), protoArgument.getAuthor().getDialogStatus().getBetId());
        Argument argument = ArgumentConverter.toArgument(protoArgument, bet);
        if (true // TODO: Надо уметь отключать и включать шифрование по дополнительному меню Шифрование:
            // [ввести ключ шифрования/ заменить ключ шифрования], отключить шифрование/включить шифрование.
                // если у обоих выключено шифрование, то не шифровать вообще,
            // если у одного включено, а у второго выключено, то шифровать
            // если шифровать парный ключ стандартным ключом то спаливается парный ключ - это надо решить.
        ) {
            User author;
            User receiver;
            if (argument.getBetRole().equals(BetRole.INITIATOR)) {
                author = argument.getBet().getInitiator();
                receiver = argument.getBet().getOpponent();
            } else {
                author = argument.getBet().getOpponent();
                receiver = argument.getBet().getInitiator();
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
