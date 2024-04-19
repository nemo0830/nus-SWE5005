package com.nus.team3.controller;

import com.nus.team3.dto.PositionDto;
import com.nus.team3.dto.StockLiveDto;
import com.nus.team3.entity.GeneralMessageEntity;
import com.nus.team3.entity.PositionPageEntity;
import com.nus.team3.service.PositionService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "PositionController", description = "PositionController")
@Slf4j
@RestController
@RequestMapping("/api")
public class PositionController {

  private final PositionService positionService;

  @Autowired
  public PositionController(PositionService positionService) {
    this.positionService = positionService;
  }

  @GetMapping("/stocklive")
  public GeneralMessageEntity<List<StockLiveDto>> getStockLive() {
    return GeneralMessageEntity.ok(this.positionService.getStockLive());
  }

  @GetMapping("/position")
  public GeneralMessageEntity<List<PositionDto>> getAllPositions(
      @RequestParam(required = false) Integer userId) {
    if (!Optional.ofNullable(userId).isPresent()) {
      return GeneralMessageEntity.ok(this.positionService.getAllPosition());
    }

    return GeneralMessageEntity.ok(this.positionService.getAllPositionByUserId(userId));
  }

  @GetMapping("/position/summary")
  public GeneralMessageEntity<List<PositionPageEntity>> getAllPositionSummary(
      @RequestParam(required = false) Integer userId) {

    if (null == userId) {
      throw new RuntimeException("Please provide a userId");
    }

    return null;
  }

}
