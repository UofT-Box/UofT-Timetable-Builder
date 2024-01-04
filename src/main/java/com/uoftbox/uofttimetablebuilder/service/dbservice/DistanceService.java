package com.uoftbox.uofttimetablebuilder.service.dbservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uoftbox.uofttimetablebuilder.repository.distance.DistancesRepository;

@Service
public class DistanceService {

    @Autowired
    private DistancesRepository distancesRepository;

    public Integer getDistance(String origin, String destination) {

        Integer distance = distancesRepository.findDistanceByLocation(origin, destination);
        return distance != null ? distance : -1; // 如果未找到距离，返回 -1
        
    }
}
