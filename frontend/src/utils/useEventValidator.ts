// useValidate composable
// used for input validation and showing errors
import { computed, reactive, ref, watch } from "vue";
import { EventResponse } from "../gen-types";
import { getEventsByCategoryIdOnDate } from "../service/api";
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

function validateStartTime(startTime: string, duration: number, events: EventResponse[], currentEventId: string | null = null): ValidationResult<{ hasOverlappingEvents: boolean }> {
  const errors: string[] = [];

  const now = new Date();
  const startTimeDate = new Date(startTime);

  if (startTimeDate.getTime() <= now.getTime()) {
    errors.push("Start time must be in the future");
  }

  const overlapEvents = findOverlap(startTime, duration, events, currentEventId);
  const hasOverlap = overlapEvents.length > 0;

  return {
    ...makeValidateResult(errors),
    hasOverlappingEvents: hasOverlap,
  };
}

type InputError = string[] | false;

interface Errors {
  bookingName: InputError;
  bookingEmail: InputError;
  eventNotes: InputError;
  eventStartTime: InputError;
  hasOverlappingEvents: boolean;
}

interface Options {
  getDurationByCategoryId: (categoryId: number) => number | null;
}
export function useEventValidator(options: Options) {
  const { getDurationByCategoryId: onCategoryChange } = options;

  const defaultErrors: Errors = {
    bookingName: false,
    bookingEmail: false,
    eventNotes: false,
    eventStartTime: false,
    hasOverlappingEvents: false,
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

  // const eventsInCategoryOnDate = ref<EventResponse[]>([]);

  // const eventId = ref("");

  // function setEventId(id: string) {
  //   eventId.value = id;
  // }

  // function setCategoryId(id: number) {
  //   inputs.eventCategoryId = id;
  // }


  // use watch instead of event handlers
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

    const events = await getEventsByCategoryIdOnDate(inputs.eventCategoryId, dateMidnight.toISOString());
    const result = validateStartTime(startTime, duration.value, events);
    errors.eventStartTime = result.valid ? false : result.errors;
    errors.hasOverlappingEvents = result.hasOverlappingEvents;
  });

  watch(() => inputs.eventCategoryId, async (categoryId, prevCategoryId) => {
    if (categoryId === prevCategoryId) {
      return;
    }

    const _duration = onCategoryChange(categoryId);
    if (_duration === null) {
      return;
    }

    duration.value = _duration;

    const dateMidnight = new Date(inputs.eventStartTime);
    if (isNaN(dateMidnight.getTime())) {
      return;
    }
    
    dateMidnight.setHours(0, 0, 0, 0);

    const events = await getEventsByCategoryIdOnDate(categoryId, dateMidnight.toISOString());
    const result = validateStartTime(inputs.eventStartTime, _duration, events);
    errors.eventStartTime = result.valid ? false : result.errors;
    errors.hasOverlappingEvents = result.hasOverlappingEvents;
  });

  const hasErrors = computed(() => {
    return Object.values(errors).some((error) => error !== false);
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