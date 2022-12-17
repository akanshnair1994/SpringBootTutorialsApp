package com.example.tutorialapplication.Controller;

import com.example.tutorialapplication.Model.Tutorial;
import com.example.tutorialapplication.Repository.TutorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestController
public class TutorialController {
    @Autowired
    TutorialRepository repo;

    @GetMapping("/tutorials")
    public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
        try {
            List<Tutorial> tutorials = new ArrayList<>();

            if (title == null)
                repo.findAll().forEach(tutorials::add);
            else
                repo.findByTitle(title).forEach(tutorials::add);

            if (tutorials.isEmpty()) {
                return new ResponseEntity<>(NO_CONTENT);
            }

            return new ResponseEntity<>(tutorials, OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
        Optional<Tutorial> tutorial = repo.findById(id);

        return tutorial.map(value -> new ResponseEntity<>(value, OK)).orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @PostMapping("/tutorials")
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
        try {
            String title = tutorial.getTitle().toLowerCase();
            List<String> titles = new ArrayList<>();

            List<Tutorial> tutorialList = new ArrayList<>(repo.findAll());
            tutorialList.forEach(tut -> titles.add(tut.getTitle().toLowerCase()));
            boolean titleExist = titles.contains(title);

            if (!titleExist) {
                Tutorial _tutorial = repo
                        .save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
                return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(null, CONFLICT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") Long id, @RequestBody Tutorial tutorial) {
        Optional<Tutorial> tutorialToChange = repo.findById(id);

        if (tutorialToChange.isPresent()) {
            Tutorial newTutorial = tutorialToChange.get();
            newTutorial.setTitle(tutorial.getTitle());
            newTutorial.setDescription(tutorial.getDescription());
            newTutorial.setPublished(tutorial.isPublished());

            return new ResponseEntity<>(repo.save(newTutorial), OK);
        } else {
            return new ResponseEntity<>(NOT_FOUND);
        }
    }

    @DeleteMapping("/tutorial/{id}")
    public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") Long id) {
        try {
            repo.deleteById(id);
            return new ResponseEntity<>(NO_CONTENT);
        } catch (Exception ex) {
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/tutorials")
    public ResponseEntity<HttpStatus> deleteAllTutorials() {
        try {
            repo.deleteAll();
            return new ResponseEntity<>(NO_CONTENT);
        } catch (Exception ex) {
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tutorials/published")
    public ResponseEntity<List<Tutorial>> findByStatus() {
        try {
            List<Tutorial> tutorials = repo.findByPublished(true);

            if (tutorials.isEmpty())
                return new ResponseEntity<>(NO_CONTENT);
            return new ResponseEntity<>(tutorials, OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }
}
