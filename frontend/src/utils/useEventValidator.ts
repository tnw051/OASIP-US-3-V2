// useValidate composable
// used for input validation and showing errors
import { errors } from "jose";
import { computed, reactive, ref } from "vue";
import { EventResponse } from "../gen-types";
import { getEventsByCategoryIdOnDate } from "../service/api";
import { findOverlap } from "./index";
import { makeValidateResult, ValidationResult } from "./validators/common";

const defaultValue = "";

export function useEventValidatorOld() {
  function makeDefaultValues() {
    return {
      bookingName: defaultValue,
      bookingEmail: defaultValue,
      eventStartTime: defaultValue,
      eventCategoryId: 0,
      eventNotes: defaultValue,
    };
  }

  const errors = ref<{
    bookingName?: string[];
    bookingEmail?: string[];
    eventStartTime?: string[];
    eventCategoryId?: string[];
    eventNotes?: string[];
    hasOverlappingEvents?: boolean;
  }>({
    bookingName: [],
    bookingEmail: [],
    eventStartTime: [],
    eventNotes: [],
    hasOverlappingEvents: false,
  });

  const inputs = ref<{
    bookingName?: string;
    bookingEmail?: string;
    eventStartTime?: string;
    eventCategoryId?: number;
    eventNotes?: string;
  }
  // | {
  //   eventStartTime: string,
  //   eventNotes: string,
  //   eventCategoryId?: number,
  // }
  >(makeDefaultValues());

  const eventDuration = ref<number | null>(null);
  const eventsForSelectedCategoryAndDate = ref<EventResponse[]>([]);
  const eventId = ref<number | null>(null);

  function setEventDuration(duration: number) {
    eventDuration.value = duration;
  }

  function setEventId(id: number) {
    eventId.value = id;
  }

  function setCategoryId(id: number) {
    inputs.value.eventCategoryId = id;
  }

  function resetInputs() {
    Object.keys(inputs.value).forEach(key => {
      inputs.value[key] = defaultValue;
    });
  }


  function validateBookingName(e) {
    const bookingName = e.target.value;
    errors.value.bookingName = [];

    if (bookingName.length > 100) {
      errors.value.bookingName.push("Booking name must be less than 100 characters");
    }

    if (bookingName.trim().length === 0) {
      errors.value.bookingName.push("Booking name must not be blank");
    }
  }


  function validateBookingEmail(e) {
    const bookingEmail = e.target.value;
    errors.value.bookingEmail = [];

    if (bookingEmail.length > 50) {
      errors.value.bookingEmail.push("Booking email must be less than 50 characters");
    }

    if (bookingEmail.trim().length === 0) {
      errors.value.bookingEmail.push("Booking email must not be blank");
    }

    // RFC2822 https://regexr.com/2rhq7
    const emailRegex = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
    if (!emailRegex.test(bookingEmail)) {
      errors.value.bookingEmail.push("Booking email is invalid");
    }
  }


  function validateEventNotes(e) {
    const eventNotes = e.target.value;
    errors.value.eventNotes = [];

    if (eventNotes.length > 500) {
      errors.value.eventNotes.push("Booking note must be less than 500 characters");
    }
  }


  const previousDate = ref<string | null>(null);

  async function validateStartTime() {
    const eventStartTime = inputs.value.eventStartTime;
    const eventCategoryId = inputs.value.eventCategoryId;

    if (!eventStartTime) {
      return;
    }

    const now = new Date();
    const startTime = new Date(eventStartTime);

    errors.value.eventStartTime = [];
    errors.value.hasOverlappingEvents = false;

    if (startTime.getTime() <= now.getTime()) {
      errors.value.eventStartTime.push("Start time must be in the future");
    }

    const date = eventStartTime.split("T")[0];
    if (date !== previousDate.value) {
      console.log("date changed", date);

      if (eventCategoryId) {
        const dateMidnight = new Date(eventStartTime);
        dateMidnight.setHours(0, 0, 0, 0);

        eventsForSelectedCategoryAndDate.value = await getEventsByCategoryIdOnDate(eventCategoryId, dateMidnight.toISOString());
        console.log("fetched events (start time changed)", eventsForSelectedCategoryAndDate.value);
      }
    }

    previousDate.value = date;

    if (eventCategoryId) {
      const overlapEvents = findOverlap(eventStartTime, eventDuration.value, eventsForSelectedCategoryAndDate.value, eventId.value);
      const hasOverlap = overlapEvents.length > 0;

      if (hasOverlap) {
        errors.value.hasOverlappingEvents = true;
      }
    }
  }


  async function validateCategoryId() {
    const eventStartTime = inputs.value.eventStartTime;
    const eventCategoryId = inputs.value.eventCategoryId;

    if (!eventStartTime || !eventCategoryId) {
      return;
    }

    const dateMidnight = new Date(eventStartTime);
    dateMidnight.setHours(0, 0, 0, 0);

    eventsForSelectedCategoryAndDate.value = await getEventsByCategoryIdOnDate(eventCategoryId, dateMidnight.toISOString());
    console.log("fetched events (category id changed)", eventsForSelectedCategoryAndDate.value);

    const overlapEvents = findOverlap(eventStartTime, eventDuration.value, eventsForSelectedCategoryAndDate.value);
    const hasOverlap = overlapEvents.length > 0;

    if (hasOverlap) {
      errors.value.hasOverlappingEvents = true;
    } else {
      errors.value.hasOverlappingEvents = false;
    }
  }

  const canSubmit = computed(() => {
    const noErrors = Object.values(errors.value).every((error) => error === false || (Array.isArray(error) && error.length === 0));
    const inputsWithoutNotes = { ...inputs.value };
    delete inputsWithoutNotes.eventNotes;

    const noEmptyFields = Object.values(inputsWithoutNotes).every((value) => value !== "");

    return noErrors && noEmptyFields;
  });

  return {
    errors,
    inputs,
    setEventDuration,
    setEventId,
    eventsForSelectedCategoryAndDate,
    validateBookingName,
    validateBookingEmail,
    validateEventNotes,
    validateStartTime,
    validateCategoryId,
    resetInputs,
    canSubmit,
    setCategoryId,
  };
}

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

export function useEventValidator() {
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

  const eventsForSelectedCategoryAndDate = ref<EventResponse[]>([]);

  const prevDateMidnight = ref<Date>();

  const eventId = ref("");

  // function setEventId(id: string) {
  //   eventId.value = id;
  // }

  // function setCategoryId(id: number) {
  //   inputs.eventCategoryId = id;
  // }

  async function handleBookingNameChange(e: Event) {
    const target = e.target as HTMLInputElement;
    const name = target.value;
    const result = validateBookingName(name);
    errors.bookingName = result.errors;
  }

  async function handleBookingEmailChange(e: Event) {
    const target = e.target as HTMLInputElement;
    const email = target.value;
    const result = validateBookingEmail(email);
    errors.bookingEmail = result.errors;
  }

  async function handleEventNotesChange(e: Event) {
    const target = e.target as HTMLInputElement;
    const notes = target.value;
    const result = validateEventNotes(notes);
    errors.eventNotes = result.errors;
  }

  async function handleEventStartTimeChange(e: Event, duration: number, currentEventId: string | null = null) {
    const target = e.target as HTMLInputElement;
    const startTime = target.value;
    const events = await getEventsByCategoryIdAndDate(new Date(startTime));
    const result = validateStartTime(startTime, duration, events, currentEventId);
    errors.eventStartTime = result.errors;
  }

  async function handleEventCategoryIdChange(duration: number, currentEventId: string | null = null) {
    const events = await getEventsByCategoryIdAndDate(new Date(inputs.eventStartTime));
    const startTime = inputs.eventStartTime;
    const result = validateStartTime(startTime, duration, events, currentEventId);
    errors.eventStartTime = result.errors;
  }

  // update eventsForSelectedCategoryAndDate when start time or category id changed
  async function getEventsByCategoryIdAndDate(date: Date): Promise<EventResponse[]> {
    const startTime = inputs.eventStartTime;
    const categoryId = inputs.eventCategoryId;
    if (!startTime || !categoryId) {
      return [];
    }

    const dateMidnight = new Date(date);
    dateMidnight.setHours(0, 0, 0, 0);
    if (prevDateMidnight.value?.getTime() === dateMidnight.getTime()) {
      return eventsForSelectedCategoryAndDate.value;
    }

    const utcDateMidnightString = dateMidnight.toISOString();
    const events = await getEventsByCategoryIdOnDate(categoryId, utcDateMidnightString);
    eventsForSelectedCategoryAndDate.value = events;

    prevDateMidnight.value = dateMidnight;

    return events;
  }

  function resetInputsAndErrors() {
    Object.assign(inputs, defaultInputs);
    Object.assign(errors, defaultErrors);
  }

  return {
    errors,
    inputs,
    handleBookingNameChange,
    handleBookingEmailChange,
    handleEventNotesChange,
    handleEventStartTimeChange,
    handleEventCategoryIdChange,
    resetInputsAndErrors,
  };
}