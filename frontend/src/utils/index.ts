import { EventTimeSlotResponse } from "../gen-types";
import { Id } from "../types";

export const sortDirections = {
  ASC: "asc",
  DESC: "desc",
};

export function sortByDateInPlace(arr, dateExtractor, direction = sortDirections.DESC) {
  if (direction === sortDirections.DESC) {
    return arr.sort((a, b) => {
      return new Date(dateExtractor(b)).getTime() - new Date(dateExtractor(a)).getTime();
    });
  } else {
    return arr.sort((a, b) => {
      return new Date(dateExtractor(a)).getTime() - new Date(dateExtractor(b)).getTime();
    });
  }
}

export function formatDateTimeLocal(date) {
  const copiedDate = new Date(date);
  const offset = copiedDate.getTimezoneOffset();
  // add the offset before converting to UTC, result in local time
  // offset is -420 min. for UTC+7, thus --420 = +420
  copiedDate.setMinutes(copiedDate.getMinutes() - offset, 0, 0);
  return copiedDate.toISOString().slice(0, -1);
}

export function formatDateAndFromToTime(date, durationMinute) {
  const from = new Date(date);
  const to = new Date(date);
  to.setMinutes(to.getMinutes() + durationMinute);
  return `${formatDate(from)} ${formatTime(from)} - ${formatTime(to)}`;
}

export function formatDateTime(date) {
  return `${formatDate(date)} ${formatTime(date)} `;
}

export function formatTime(date) {
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

export function formatDate(date) {
  return date.toLocaleDateString([], {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

// [
//   {
//       "eventStartTime": "2022-10-24T02:00:00Z",
//       "eventEndTime": "2022-10-24T02:30:00Z",
//       "eventDuration": 30,
//       "eventCategoryId": 1
//   },
//   {
//       "eventStartTime": "2022-10-24T02:30:00Z",
//       "eventEndTime": "2022-10-24T03:00:00Z",
//       "eventDuration": 30,
//       "eventCategoryId": 1
//   },
//   {
//       "eventStartTime": "2022-10-24T03:00:00Z",
//       "eventEndTime": "2022-10-24T03:30:00Z",
//       "eventDuration": 30,
//       "eventCategoryId": 1
//   },
//   {
//       "eventStartTime": "2022-10-24T03:30:00Z",
//       "eventEndTime": "2022-10-24T04:00:00Z",
//       "eventDuration": 30,
//       "eventCategoryId": 1
//   },
//   {
//       "eventStartTime": "2022-10-24T04:00:00Z",
//       "eventEndTime": "2022-10-24T04:30:00Z",
//       "eventDuration": 30,
//       "eventCategoryId": 1
//   },
//   {
//       "eventStartTime": "2022-10-24T04:30:00Z",
//       "eventEndTime": "2022-10-24T05:00:00Z",
//       "eventDuration": 30,
//       "eventCategoryId": 1
//   }
// ]

// use the above example to find overlapping events below

export function findOverlap(eventStartTime: Date, duration: number, existingEvents: EventTimeSlotResponse[]) {
  console.log("findOverlap", eventStartTime, duration, existingEvents);
  const startTime = new Date(eventStartTime);
  const endTime = new Date(startTime);
  endTime.setMinutes(startTime.getMinutes() + duration);
  const formatter = Intl.DateTimeFormat([], { dateStyle: "medium", timeStyle: "short" });

  console.log(`=== checking overlap for ${formatter.format(startTime)} | ${formatter.format(endTime)} ===`);

  const overlapEvents = existingEvents.filter(event => {
    const otherStartTime = new Date(event.eventStartTime);
    const otherEndTime = new Date(event.eventStartTime);

    // all overlap events. there are two scenarios:
    // 1. events that started before the startTime and ended after the startTime
    // 2. events that started between the startTime (inclusive) and the endTime (exclusive)
    const isPastOverlap = otherStartTime.getTime() < startTime.getTime() && otherEndTime.getTime() > startTime.getTime();
    const isFutureOverlap = otherStartTime.getTime() >= startTime.getTime() && otherStartTime.getTime() < endTime.getTime();

    if (isPastOverlap || isFutureOverlap) {
      if (isPastOverlap) {
        console.log("> type: past overlap");
      }
      if (isFutureOverlap) {
        console.log("> type: future overlap");
      }

      console.log(`startTime: ${formatter.format(startTime)} | endTime: ${formatter.format(endTime)}`);
      console.log(`otherStartTime: ${formatter.format(otherStartTime)} | otherEndTime: ${formatter.format(otherEndTime)}`);
      return true;
    }

    return false;
  });

  if (overlapEvents.length === 0) {
    console.log(`no overlap at ${formatter.format(startTime)}`);
  }

  return overlapEvents;
}

export const inputConstraits = {
  MAX_DATE: "9999-12-31",
  MAX_DATETIME_LOCAL: "9999-12-31T23:59",
};