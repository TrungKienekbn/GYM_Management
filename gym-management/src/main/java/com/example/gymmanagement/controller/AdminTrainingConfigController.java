package com.example.gymmanagement.controller;
import com.example.gymmanagement.enums.*;
import com.example.gymmanagement.service.TrainingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/admin/training-config") @RequiredArgsConstructor
public class AdminTrainingConfigController {
 private final TrainingConfigService service;
 @GetMapping("/schedules") public List<Map<String,Object>> schedules(){return service.schedules();}
 @PutMapping("/schedules/{sessions}") public Map<String,Object> saveSchedule(@PathVariable int sessions,@RequestBody ScheduleRequest b){return service.saveSchedule(sessions,b.days());}
 @GetMapping("/splits") public Map<String,Object> split(@RequestParam Goal goal,@RequestParam int sessions){return service.split(goal,sessions);}
 @PutMapping("/splits") public Map<String,Object> saveSplit(@RequestBody SplitRequest b){return service.saveSplit(b.goal(),b.sessionsPerWeek(),b.dayGroups());}
 public record ScheduleRequest(List<Integer> days){}
 public record SplitRequest(Goal goal,Integer sessionsPerWeek,List<List<MuscleGroup>> dayGroups){}
}
