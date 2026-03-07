package com.project.baedalsodae.store.entity;

import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "p_store_hours",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_store_hours_day",
                    columnNames = {"store_id", "day_of_week"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreHours {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_store_hours_store"))
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 3, nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "break_start")
    private LocalTime breakStart;

    @Column(name = "break_end")
    private LocalTime breakEnd;

    @Column(name = "is_open", nullable = false)
    @ColumnDefault("true")
    private boolean isOpen = true;

    private StoreHours(
            Store store,
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            LocalTime breakStart,
            LocalTime breakEnd,
            boolean isOpen) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.isOpen = isOpen;
    }

    public static StoreHours createStoreHours(
            Store store,
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            LocalTime breakStart,
            LocalTime breakEnd,
            boolean isOpen) {
        return new StoreHours(store, dayOfWeek, openTime, closeTime, breakStart, breakEnd, isOpen);
    }

    public void update(
            LocalTime openTime,
            LocalTime closeTime,
            LocalTime breakStart,
            LocalTime breakEnd,
            boolean isOpen) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.isOpen = isOpen;
    }
}