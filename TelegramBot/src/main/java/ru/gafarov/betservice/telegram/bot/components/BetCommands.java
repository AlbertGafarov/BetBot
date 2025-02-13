package ru.gafarov.betservice.telegram.bot.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import ru.gafarov.betservice.telegram.bot.actions.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BetCommands {
    private final Action startAction;
    private final Action createAction;
    private final Action draftAction;
    private final Action codeAction;
    private final Action showBetsAction;
    private final Action newStatusBetAction;
    private final Action showBetAction;
    private final Action closeAction;
    private final Action myReferenceAction;
    private final Action addMeAction;
    private final Action friendsAction;
    private final Action infoAction;
    private final Action argumentAction;
    private final Action cancelAction;
//    private final Action setSecretKeyAction;
    private final Action secretAction;
    @Getter
    private final Map<String, String> botCommands = new HashMap<>(){{
        put("/info", "Информация о боте");
        put("/start", "start bot");
        put("/create", "Новый спор");
        put("/draft", "Черновик");
        put("/code", "Мой код");
        put("/bets", "Мои споры");
        put("/reference", "Моя ссылка");
        put("/friends", "Мои друзья");
        put("/secret", "Шифрование");
    }};

    public List<BotCommand> getBotCommandList(){
        return botCommands.entrySet().stream().map(e -> new BotCommand(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
    public Map<String, Action> getActions() {
        return new HashMap<>(){{
            put("info", infoAction);
            put("start", startAction);
            put("create", createAction);
            put("draft", draftAction);
            put("code", codeAction);
            put("bets", showBetsAction);
            put("reference", myReferenceAction);
            put("addMe", addMeAction);
            put("friends", friendsAction);
            put("draftBet", draftAction);
            put("newStatus", newStatusBetAction);
            put("showBet", showBetAction);
            put("close", closeAction);
            put("argument", argumentAction);
            put("cancel", cancelAction);
            put("secret", secretAction);
        }};
    }
}