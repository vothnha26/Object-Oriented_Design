package com.alotra.service.command;

import com.alotra.entity.Topping;
import com.alotra.entity.enums.ToppingStatus;
import com.alotra.repository.ToppingRepository;

public class UpdateToppingStatusCommand implements AdminCommand {
    private final ToppingRepository repository;
    private final Integer toppingId;
    private final ToppingStatus newStatus;
    private ToppingStatus previousStatus;

    public UpdateToppingStatusCommand(ToppingRepository repository, Integer toppingId, ToppingStatus newStatus) {
        this.repository = repository;
        this.toppingId = toppingId;
        this.newStatus = newStatus;
    }

    @Override
    public void execute() {
        Topping t = repository.findById(toppingId).orElseThrow();
        this.previousStatus = t.getStatus();
        t.setStatus(newStatus);
        repository.save(t);
    }

    @Override
    public void undo() {
        Topping t = repository.findById(toppingId).orElseThrow();
        t.setStatus(previousStatus);
        repository.save(t);
    }

    @Override
    public String getDescription() {
        return "Cập nhật trạng thái Topping #" + toppingId + " thành " + newStatus;
    }
}
