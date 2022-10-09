// useValidate composable
// used for input validation and showing errors
import { computed, ref } from "vue";
import { EventResponse } from "../gen-types";
import { getEventsByCategoryIdOnDate } from "../service/api";
import { findOverlap } from "./index";

const defaultValue = "";

export function useEventValidator() {
  function makeDefaultValues() {
    return {
      bookingName: defaultValue,
      bookingEmail: defaultValue,
      eventStartTime: defaultValue,
      eventCategoryId: 0,
      eventNotes: defaultValue,
    };
  }

  const errors = ref({
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


  const previousDate = ref(null);

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