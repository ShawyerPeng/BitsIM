package controller;

import model.OfflineMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import service.OfflineMessageService;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/offlineMessage")
public class OfflineMessageController {
    @Autowired
    private OfflineMessageService offlineMessageService;

    @RequestMapping(value = "/insert")
    @ResponseBody
    public Integer insert(@RequestBody OfflineMessage offlineMessage) {
        return offlineMessageService.insertOfflineMessage(offlineMessage);
    }

    @RequestMapping(value = "/selectAll")
    @ResponseBody
    public void selectAll(@RequestBody OfflineMessage offlineMessage) {
        offlineMessageService.insertOfflineMessage(offlineMessage);
    }

    @RequestMapping(value = "/selectByTime")
    @ResponseBody
    public List<OfflineMessage> selectByTime(@RequestParam("startTime") Date startTime, @RequestParam("endTime") Date endTime) {
        return offlineMessageService.selectOfflineMessage(startTime, endTime);
    }
}