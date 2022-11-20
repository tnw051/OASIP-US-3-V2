// useValidate composable
// used for input validation and showing errors
import { computed, reactive, ref, watch } from "vue";
import { EditEventRequest, EventResponse, EventTimeSlotResponse } from "../gen-types";
import { getAllocatedTimeSlotsInCategoryOnDate } from "../service/api";
import { EventTimeSlot } from "../types";
import { findOverlap, formatDateTimeLocal } from "./index";
import { makeValidateResult, ValidationResult } from "./validators/common";

// refactor the useEventValidator to pure functions and later use it in the composition API
function validateBookingName(name: string): ValidationResult {
  const errors: string[] = [];

  if (name.length > 100) {
    errors.push("Booking name must be less than 100 characters");
  }

  if (name.trim().length === 0) {
    errors.push("Booking name must not be blank");
  }

  return makeValidateResult(errors);
}

function validateBookingEmail(email: string): ValidationResult {
  const errors: string[] = [];

  if (email.length > 50) {
    errors.push("Booking email must be less than 50 characters");
  }

  if (email.trim().length === 0) {
    errors.push("Booking email must not be blank");
  }

  // RFC2822 https://regexr.com/2rhq7
  const emailRegex = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
  if (!emailRegex.test(email)) {
    errors.push("Booking email is invalid");
  }

  return makeValidateResult(errors);
}

function validateEventNotes(notes: string): ValidationResult {
  const errors: string[] = [];

  if (notes.length > 500) {
    errors.push("Booking note must be less than 500 characters");
  }

  return makeValidateResult(errors);
}

function validateStartTime(startTime: Date, duration: number, events: EventTimeSlotResponse[], excludeTimeSlot?: EventTimeSlot): ValidationResult<{ hasOverlappingEvents: boolean }> {
  console.log("getting allocated time slots");

  const errors: string[] = [];

  const now = new Date();
  const startTimeDate = new Date(startTime);

  if (startTimeDate.getTime() <= now.getTime()) {
    errors.push("Start time must be in the future");
  }

  const overlapEvents = findOverlap(startTime, duration, events, excludeTimeSlot);
  const hasOverlap = overlapEvents.length > 0;

  if (hasOverlap) {
    errors.push("Start time overlaps with other event(s)");
  }

  return {
    ...makeValidateResult(errors),
    hasOverlappingEvents: hasOverlap,
  };
}

// initial: false means the field is optional, undefined means the field is required
// normal: false means no error, undefined means not validated yet
type InputError = string[] | false | undefined;

interface Errors {
  bookingName: InputError;
  bookingEmail: InputError;
  eventNotes: InputError;
  eventStartTime: InputError;
  hasOverlappingEvents: boolean | undefined;
}

interface Options {
  getDurationByCategoryId?: (categoryId: number) => number | null;
  currentEvent?: EventResponse;
}

export function useEventValidator(options: Options) {
  const { getDurationByCategoryId, currentEvent } = options;

  let currentTimeSlot: EventTimeSlot;
  if (currentEvent) {
    const { eventStartTime, eventDuration, eventCategory } = currentEvent;
    const _startTime = new Date(eventStartTime);
    const _endTime = new Date(_startTime);
    _endTime.setMinutes(_endTime.getMinutes() + eventDuration);

    currentTimeSlot = {
      eventStartTime: _startTime,
      eventEndTime: _endTime,
      eventDuration,
      eventCategoryId: Number(eventCategory.id),
    };
  }

  const defaultErrors: Errors = {
    bookingName: false,
    bookingEmail: false,
    eventNotes: false,
    eventStartTime: false,
    hasOverlappingEvents: false,
  };
  const errors = reactive<Errors>(defaultErrors);

  const defaultTextValue = "";
  const defaultIntValue = 0;
  const defaultInputs = {
    bookingName: currentEvent?.bookingName ?? defaultTextValue,
    bookingEmail: currentEvent?.bookingEmail ?? defaultTextValue,
    eventNotes: currentEvent?.eventNotes ?? defaultTextValue,
    eventStartTime: currentEvent?.eventStartTime ? formatDateTimeLocal(currentEvent.eventStartTime) : defaultTextValue,
    eventCategoryId: Number(currentEvent?.eventCategory?.id ?? defaultIntValue),
  };
  const inputs = reactive({ ...defaultInputs });

  const duration = ref<number>();
  if (currentEvent) {
    duration.value = currentEvent.eventDuration;
  }

  watch(() => inputs.bookingName, (name) => {
    if (inputs.bookingName === defaultTextValue) {
      return;
    }

    const result = validateBookingName(name);
    errors.bookingName = result.valid ? false : result.errors;
  });

  watch(() => inputs.bookingEmail, (email) => {
    if (inputs.bookingEmail === defaultTextValue) {
      return;
    }

    const result = validateBookingEmail(email);
    errors.bookingEmail = result.valid ? false : result.errors;
  });

  watch(() => inputs.eventNotes, (notes) => {
    if (inputs.eventNotes === defaultTextValue) {
      return;
    }

    const result = validateEventNotes(notes);
    errors.eventNotes = result.valid ? false : result.errors;
  });

  watch(() => inputs.eventStartTime, async (startTime, prevStartTime) => {
    if (inputs.eventStartTime === defaultTextValue || !duration.value) {
      return;
    }

    const prevDateMidnight = new Date(prevStartTime);
    prevDateMidnight.setHours(0, 0, 0, 0);
    const dateMidnight = new Date(startTime);
    dateMidnight.setHours(0, 0, 0, 0);
    if (prevDateMidnight.getTime() === dateMidnight.getTime()) {
      return;
    }

    const events = await getAllocatedTimeSlotsInCategoryOnDate(inputs.eventCategoryId, dateMidnight);
    const result = validateStartTime(new Date(startTime), duration.value, events, currentTimeSlot);
    errors.eventStartTime = result.valid ? false : result.errors;
    errors.hasOverlappingEvents = result.hasOverlappingEvents;
  });

  watch(() => inputs.eventCategoryId, async (categoryId, prevCategoryId) => {
    if (inputs.eventCategoryId === defaultIntValue ||
      categoryId === prevCategoryId ||
      currentTimeSlot === undefined && getDurationByCategoryId === undefined) {
      return;
    }

    let _duration: number | null = null;
    if (currentTimeSlot?.eventDuration) {
      _duration = currentTimeSlot.eventDuration;
    }

    if (getDurationByCategoryId) {
      _duration = getDurationByCategoryId(categoryId);
    }
    if (_duration === null) {
      console.log(`duration is null for category ${categoryId}`);
      return;
    }

    duration.value = _duration;

    const dateMidnight = new Date(inputs.eventStartTime);
    if (isNaN(dateMidnight.getTime())) {
      return;
    }

    dateMidnight.setHours(0, 0, 0, 0);

    const events = await getAllocatedTimeSlotsInCategoryOnDate(categoryId, dateMidnight);
    const result = validateStartTime(new Date(inputs.eventStartTime), _duration, events, currentTimeSlot);
    errors.eventStartTime = result.valid ? false : result.errors;
    errors.hasOverlappingEvents = result.hasOverlappingEvents;
  });

  const hasErrors = computed(() => {
    return Object.entries(errors).some(([key, value]) => {
      return value !== false;
    });
  });

  const hasChanges = computed(() => {
    if (currentEvent === undefined) {
      return false;
    }

    return inputs.eventCategoryId !== currentEvent.eventCategory.id ||
      inputs.bookingName !== currentEvent.bookingName ||
      inputs.bookingEmail !== currentEvent.bookingEmail ||
      inputs.eventNotes !== currentEvent.eventNotes ||
      formatDateTimeLocal(inputs.eventStartTime) !== formatDateTimeLocal(currentEvent.eventStartTime);
  });

  const canSubmit = computed(() => {
    if (currentEvent) {
      return !hasErrors.value && hasChanges.value;
    }

    return !hasErrors.value;
  });

  function resetInputsAndErrors() {
    Object.assign(inputs, defaultInputs);
    Object.assign(errors, defaultErrors);
  }

  return {
    errors,
    inputs,
    resetInputsAndErrors,
    hasErrors,
    hasChanges,
    canSubmit,
  };
}