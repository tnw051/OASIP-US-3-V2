// useValidate composable
// used for input validation and showing errors
import { computed, reactive, ref, watch } from "vue";
import { EventTimeSlotResponse } from "../gen-types";
import { getAllocatedTimeSlotsInCategoryOnDate } from "../service/api";
import { EventTimeSlot } from "../types";
import { findOverlap } from "./index";
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
  currentTimeSlot?: EventTimeSlot;
  exclude?: Partial<{
    bookingName: boolean;
    bookingEmail: boolean;
  }>;
}

export function useEventValidator(options: Options) {
  const { getDurationByCategoryId, currentTimeSlot, exclude } = options;

  let optionalFields = {};
  if (exclude) {
    optionalFields = Object.keys(exclude).reduce((acc, key) => {
      if (exclude[key]) {
        acc[key] = false;
      }
      return acc;
    }, {});
  }

  const defaultErrors: Errors = {
    bookingName: undefined,
    bookingEmail: undefined,
    eventNotes: false,
    eventStartTime: undefined,
    hasOverlappingEvents: false,
    ...optionalFields,
  };
  const errors = reactive<Errors>(defaultErrors);

  const defaultInputs = {
    bookingName: "",
    bookingEmail: "",
    eventNotes: "",
    eventStartTime: "",
    eventCategoryId: 0,
  };
  const inputs = reactive(defaultInputs);

  const duration = ref<number>();

  watch(() => inputs.bookingName, (name) => {
    const result = validateBookingName(name);
    errors.bookingName = result.valid ? false : result.errors;
  });

  watch(() => inputs.bookingEmail, (email) => {
    const result = validateBookingEmail(email);
    errors.bookingEmail = result.valid ? false : result.errors;
  });

  watch(() => inputs.eventNotes, (notes) => {
    const result = validateEventNotes(notes);
    errors.eventNotes = result.valid ? false : result.errors;
  });

  watch(() => inputs.eventStartTime, async (startTime, prevStartTime) => {
    if (!duration.value) {
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
    if (categoryId === prevCategoryId ||
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
      return options.exclude?.[key] !== false && value !== false;
    });
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
  };
}