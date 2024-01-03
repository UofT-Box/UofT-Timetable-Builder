package com.uoftbox.uofttimetablebuilder.repository.distance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.uoftbox.uofttimetablebuilder.model.mysql.Distances;

@Repository
public interface DistancesRepository extends JpaRepository<Distances, Integer>{
    
    @Query("SELECT d.distance FROM Distances d WHERE d.origin = ?1 AND d.destination = ?2")
    Integer findDistanceByLocation(String origin, String destination);

}
