package com.qbit.microservice.service;

import com.qbit.microservice.entity.Tag;
import com.qbit.microservice.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public Tag create(Tag tag) {
        return tagRepository.save(tag);
    }

    public Tag update(Long id, Tag updatedTag) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tag với id = " + id));

        tag.setName(updatedTag.getName());
        tag.setDescription(updatedTag.getDescription());
        tag.setColor(updatedTag.getColor());

        return tagRepository.save(tag);
    }

    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tag với id = " + id));

        tagRepository.delete(tag);
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tag với id = " + id));
    }

    public Page<Tag> findAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return tagRepository.findAll(pageable);
        }
        return tagRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    public List<Tag> findByIds(@RequestBody List<Long> ids) {
        return tagRepository.findAllById(ids);
    }

}
