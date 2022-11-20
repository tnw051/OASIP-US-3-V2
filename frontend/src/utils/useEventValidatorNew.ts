// useValidate composable
// used for input validation and showing errors
import { toFormValidator } from "@vee-validate/zod";
import { useForm } from "vee-validate";
import { computed, ref, watch } from "vue";
import { z } from "zod";
import { EventResponse, EventTimeSlotResponse } from "../gen-types";
import { getAllocatedTimeSlotsInCategoryOnDate } from "../service/api";
import { EventTimeSlot } from "../types";
import { findOverlap, formatDateTimeLocal } from "./index";
import { makeValidateResult, ValidationResult } from "./validators/common";

function validateOverlappingStartTime(startTime: Date, duration: number, events: EventTimeSlotResponse[], excludeTimeSlot?: EventTimeSlot): ValidationResult<{ hasOverlappingEvents: boolean }> {
  const errors: string[] = [];

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

interface Options {
  getDurationByCategoryId?: (categoryId: number) => number | null;
  currentEvent?: EventResponse;
}

export function useEventValidator(options: Options) {
  const { getDurationByCategoryId, currentEvent } = options;

  let currentTimeSlot: EventTimeSlot | null = null;
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


  const defaultTextValue = "";
  const defaultIntValue = 0;

  const duration = ref<number>();
  if (currentEvent) {
    duration.value = currentEvent.eventDuration;
  }

  const eventValidationSchema = toFormValidator(
    z.object({
      bookingName: z.string().min(1, "Name is required").max(100, "Name exceeds 100 characters"),
      bookingEmail: z.string().email("Email is invalid").max(50, "Email exceeds 50 characters"),
      eventNotes: z.string().max(500, "Notes exceed 500 characters").optional(),
      eventStartTime: z.string().refine((value) => {
        const date = new Date(value);
        const now = new Date();
        return date.getTime() > now.getTime();
      }, "Start time must be in the future"),
      eventCategoryId: z.number(),
    }),
  );

  const { handleSubmit, controlledValues, setErrors, setFieldError, errors, resetForm, setValues, values  } = useForm({
    validationSchema: eventValidationSchema,
    initialValues: currentEvent ? {
      bookingName: currentEvent.bookingName,
      bookingEmail: currentEvent.bookingEmail,
      eventNotes: currentEvent.eventNotes,
      eventStartTime: formatDateTimeLocal(currentEvent.eventStartTime) ,
      eventCategoryId: Number(currentEvent.eventCategory.id),
    } : undefined,
    validateOnMount: false,
    initialErrors: undefined,
    initialTouched: undefined,
  });

  const eventsInCategoryOnDate = ref<EventTimeSlotResponse[]>([]);
  watch(eventsInCategoryOnDate, (events) => {
    console.log("@@@ eventsInCategoryOnDate changed", events);
  });
    

  const VEE_VALIDATE_TIMEOUT = 100;
  watch(() => values.eventStartTime, async (startTime, prevStartTime) => {
    console.log(`startTime: ${startTime}, prevStartTime: ${prevStartTime}`);
    console.log(`duration: ${duration.value}`);

    if (!startTime || !values.eventCategoryId) {
      return;
    }

    const prevDateMidnight = new Date(prevStartTime);
    prevDateMidnight.setHours(0, 0, 0, 0);
    const dateMidnight = new Date(startTime);
    dateMidnight.setHours(0, 0, 0, 0);
    if (prevDateMidnight.getTime() !== dateMidnight.getTime()) {
      eventsInCategoryOnDate.value = await getAllocatedTimeSlotsInCategoryOnDate(values.eventCategoryId, dateMidnight);
    }

    const result = validateOverlappingStartTime(new Date(startTime), duration.value, eventsInCategoryOnDate.value, currentTimeSlot ?? undefined);
    if (!result.valid) {
      setTimeout(() => {
        setErrors({ eventStartTime: result.errors[0] });
      }, VEE_VALIDATE_TIMEOUT);
    }
  });

  watch(() => values.eventCategoryId, async (categoryId, prevCategoryId) => {
    console.log(`categoryId: ${categoryId}, prevCategoryId: ${prevCategoryId}`);

    if (categoryId === undefined ||
      !values.eventStartTime ||
      (currentTimeSlot === null && getDurationByCategoryId === undefined)) {
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

    const startTime = values.eventStartTime;
    const dateMidnight = new Date(startTime);
    if (isNaN(dateMidnight.getTime())) {
      return;
    }

    dateMidnight.setHours(0, 0, 0, 0);

    eventsInCategoryOnDate.value = await getAllocatedTimeSlotsInCategoryOnDate(categoryId, dateMidnight);
    const result = validateOverlappingStartTime(new Date(startTime), _duration, eventsInCategoryOnDate.value, currentTimeSlot ?? undefined);
    if (!result.valid) {
      setTimeout(() => {
        setErrors({ eventStartTime: result.errors[0] });
      }, 250);
    }
  });

  const hasErrors = computed(() => {
    return Object.keys(errors.value).length > 0;
  });

  const hasChanges = computed(() => {
    if (currentEvent === undefined) {
      return false;
    }

    return values.eventCategoryId !== currentEvent.eventCategory.id ||
      values.bookingName !== currentEvent.bookingName ||
      values.bookingEmail !== currentEvent.bookingEmail ||
      values.eventNotes !== currentEvent.eventNotes ||
      formatDateTimeLocal(values.eventStartTime) !== formatDateTimeLocal(currentEvent.eventStartTime);
  });

  const canSubmit = computed(() => {
    if (currentEvent) {
      return !hasErrors.value && hasChanges.value;
    }

    return !hasErrors.value && 
      values.bookingName !== undefined &&
      values.bookingEmail !== undefined &&
      values.eventStartTime !== undefined &&
      values.eventCategoryId !== undefined;
  });

  // function resetInputsAndErrors() {
  //   // Object.assign(inputs, defaultInputs);
  //   // Object.assign(errors, defaultErrors);
  // }

  return {
    // errors,
    // resetInputsAndErrors,
    hasErrors,
    hasChanges,
    canSubmit,
    handleSubmit,
    resetForm,
    setValues, 
    setErrors,
    values,
    errors,
  };
}
