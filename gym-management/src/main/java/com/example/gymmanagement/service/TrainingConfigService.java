package com.example.gymmanagement.service;

import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.*;
import com.example.gymmanagement.repository.*;
import com.example.gymmanagement.service.plan.MuscleGroupSplitPlanner;
import com.example.gymmanagement.service.schedule.ScheduleCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class TrainingConfigService {
 private final RecommendedScheduleConfigRepository scheduleRepo;
 private final MuscleSplitConfigRepository splitRepo;

 public List<Integer> recommendedDays(int sessions) {
  return scheduleRepo.findBySessionsPerWeek(sessions)
   .map(c->Arrays.stream(c.getRecommendedDays().split(",")).map(Integer::valueOf).toList())
   .orElseGet(()->ScheduleCatalog.recommendedFor(sessions));
 }
 public List<List<MuscleGroup>> dayGroups(Goal goal,int sessions) {
  return splitRepo.findByGoalAndSessionsPerWeek(goal,sessions)
   .map(c->parseGroups(c.getDayGroups()))
   .orElseGet(()->MuscleGroupSplitPlanner.defaultDayGroupsFor(goal,sessions));
 }
 public List<Map<String,Object>> schedules() {
  List<Map<String,Object>> out=new ArrayList<>();
  for(int n=1;n<=7;n++) out.add(Map.of("sessionsPerWeek",n,"days",recommendedDays(n)));
  return out;
 }
 @Transactional public Map<String,Object> saveSchedule(int sessions,List<Integer> days) {
  if(sessions<1||sessions>7||days==null||days.size()!=sessions||new HashSet<>(days).size()!=sessions||days.stream().anyMatch(d->d<1||d>7))
   throw new IllegalArgumentException("Phải chọn đúng "+sessions+" ngày khác nhau trong tuần.");
  List<Integer> sorted=days.stream().sorted().toList();
  RecommendedScheduleConfig e=scheduleRepo.findBySessionsPerWeek(sessions).orElseGet(RecommendedScheduleConfig::new);
  e.setSessionsPerWeek(sessions); e.setRecommendedDays(sorted.stream().map(String::valueOf).collect(Collectors.joining(","))); scheduleRepo.save(e);
  return Map.of("sessionsPerWeek",sessions,"days",sorted);
 }
 public Map<String,Object> split(Goal goal,int sessions) { return Map.of("goal",goal,"sessionsPerWeek",sessions,"dayGroups",dayGroups(goal,sessions)); }
 @Transactional public Map<String,Object> saveSplit(Goal goal,int sessions,List<List<MuscleGroup>> groups) {
  if(sessions<1||sessions>7||groups==null||groups.size()!=sessions||groups.stream().anyMatch(g->g==null||g.isEmpty()))
   throw new IllegalArgumentException("Mỗi buổi phải có ít nhất một nhóm cơ và đủ số buổi đã chọn.");
  MuscleSplitConfig e=splitRepo.findByGoalAndSessionsPerWeek(goal,sessions).orElseGet(MuscleSplitConfig::new);
  e.setGoal(goal); e.setSessionsPerWeek(sessions);
  e.setDayGroups(groups.stream().map(g->g.stream().distinct().map(Enum::name).collect(Collectors.joining(","))).collect(Collectors.joining(";")));
  splitRepo.save(e); return split(goal,sessions);
 }
 private List<List<MuscleGroup>> parseGroups(String value) {
  return Arrays.stream(value.split(";",-1)).map(d->Arrays.stream(d.split(",")).filter(s->!s.isBlank()).map(MuscleGroup::valueOf).toList()).toList();
 }
}
