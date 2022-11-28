import { ref, watch } from "vue";
import { EventTimeSlotResponse } from "../gen-types";
import { getAllocatedTimeSlotsInCategoryOnDate } from "../service/api";
import { EventTimeSlot } from "../types";
import { findOverlap } from "../utils";

interface Options {
  currentTimeSlot?: EventTimeSlot;
}

export function useOverlapValidator(options?: Options) {
  const startTimeRef = ref<Date>();
  const eventCategoryIdRef= ref<number>();
  const durationRef = ref<number>();
  const isOverlapping = ref<boolean>(false);

  const { currentTimeSlot } = options || {};

  const eventsInCategoryOnDate = ref<EventTimeSlotResponse[]>([]);

  async function setStartTime(startTime: string) {
    const prevStartTime = startTimeRef.value;
    console.log(startTime);
    
    startTimeRef.value = new Date(startTime);

    console.log(`startTime: ${startTime}, prevStartTime: ${prevStartTime}`);

    if (!startTime || !eventCategoryIdRef.value) {
      return;
    }

    const prevDateMidnight = getMidnightFromDate(new Date(prevStartTime));
    const dateMidnight = getMidnightFromDate(new Date(startTime));
    if (prevDateMidnight.getTime() !== dateMidnight.getTime()) {
      eventsInCategoryOnDate.value = await getAllocatedTimeSlotsInCategoryOnDate(eventCategoryIdRef.value, dateMidnight);
    }

    isOverlapping.value = validateOverlappingStartTime(new Date(startTime), durationRef.value, eventsInCategoryOnDate.value, currentTimeSlot);
  }

  async function setEventCategory(categoryId: number, duration?: number) {
    const prevCategoryId = eventCategoryIdRef.value;
    eventCategoryIdRef.value = categoryId;
    durationRef.value = duration;

    console.log(`categoryId: ${categoryId}, prevCategoryId: ${prevCategoryId}`);

    if (!categoryId || !startTimeRef.value) {
      return;
    }

    if (currentTimeSlot === undefined && duration === undefined) {
      console.error("at least one of currentTimeSlot or duration must be provided");
      return;
    } else {
      durationRef.value = currentTimeSlot?.eventDuration ?? duration;
    }

    const startTime = new Date(startTimeRef.value);
    const dateMidnight = getMidnightFromDate(startTime);

    dateMidnight.setHours(0, 0, 0, 0);

    eventsInCategoryOnDate.value = await getAllocatedTimeSlotsInCategoryOnDate(categoryId, dateMidnight);
    isOverlapping.value= validateOverlappingStartTime(startTime, durationRef.value, eventsInCategoryOnDate.value, currentTimeSlot);
  }

    
  function validateOverlappingStartTime(startTime: Date, duration: number, events: EventTimeSlotResponse[], excludeTimeSlot?: EventTimeSlot): boolean {
    const overlapEvents = findOverlap(startTime, duration, events, excludeTimeSlot);
    return overlapEvents.length > 0;
  }

  watch(eventsInCategoryOnDate, (events) => {
    console.log("@@@ eventsInCategoryOnDate changed", events);
  });

  function getMidnightFromDate(date: Date) {
    const midnight = new Date(date);
    midnight.setHours(0, 0, 0, 0);
    return midnight;
  }

  watch(isOverlapping, (isOverlapping) => {
    console.log("isOverlapping changed", isOverlapping);
  });

  return {
    setEventCategory,
    setStartTime,
    isOverlapping,
  };
}