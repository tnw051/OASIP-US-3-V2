<script setup lang="ts">
import { computed, onBeforeMount, ref } from "vue";
import { useAuthStore } from "../auth/useAuthStore";
import Badge from "../components/Badge.vue";
import BaseTable from "../components/BaseTable.vue";
import CreateEventModal from "../components/CreateEventModal.vue";
import EditEvent from "../components/EditEvent.vue";
import EventDetails from "../components/EventDetails.vue";
import Modal from "../components/Modal.vue";
import PageLayout from "../components/PageLayout.vue";
import { CategoryResponse, EditEventRequest, EventResponse } from "../gen-types";
import {
  deleteEvent,
  getCategories,
  getEvents,
  getEventsByFilter,
  getLecturerCategories,
  updateEvent,
} from "../service/api";
import { BaseSlotProps } from "../types";
import { formatDateAndFromToTime, inputConstraits, sortByDateInPlace, sortDirections } from "../utils";
import { useEditing } from "../utils/useEditing";
import { useIsLoading } from "../utils/useIsLoading";

const { isAuthenticated, isLecturer, isAuthLoading, isGuest } = useAuthStore();

const events = ref<EventResponse[]>([]);
const categories = ref<CategoryResponse[]>([]);
const { editingState, withNoEditing, startEditing, stopEditing } = useEditing<EventResponse>();
const { isLoading, setIsLoading } = useIsLoading(true);
const isEditSuccessModalOpen = ref(false);
const isEditErrorModalOpen = ref(false);
const isCancelSuccessModalOpen = ref(false);
const isCancelErrorModalOpen = ref(false);
const isCancelConfirmModalOpen = ref(false);
const selectedEvent = ref<EventResponse | null>(null);
const search = ref("");

const filteredEvents = computed(() => {
  const keyword = search.value?.trim() || "";
  if (keyword === "") {
    return events.value;
  }

  return events.value.filter((event) => {
    return event.eventNotes.toLowerCase().includes(keyword.toLowerCase()) ||
      event.bookingName.toLowerCase().includes(keyword.toLowerCase()) ||
      event.bookingEmail.toLowerCase().includes(keyword.toLowerCase());
  });
});

function resetFilter() {
  filter.value = createDefaultFilter();
  filterEvents();
  search.value = "";
}

const isCreateEventModalOpen = ref(false);
const isCreateSuccessModalOpen = ref(false);
const isCreateErrorModalOpen = ref(false);

const eventTypes = {
  DAY: "day" as const,
  UPCOMING: "upcoming" as const,
  PAST: "past" as const,
  ALL: null,
};

const categoryTypes = {
  ALL: null,
};

const filter = ref<{
  categoryId: number | null;
  type: "day" | "upcoming" | "past" | null;
  date: string;
}>(createDefaultFilter());

function createDefaultFilter() {
  return {
    categoryId: categoryTypes.ALL,
    type: eventTypes.ALL,
    date: "",
  };
}

// only call method if and only if auth is loaded
onBeforeMount(async () => {
  if (isAuthLoading.value) {
    return;
  }

  if (isAuthenticated.value) {
    const events = await getEvents();
    setEvents(events);
    if (isLecturer.value) {
      categories.value = (await getLecturerCategories()) || [];
    } else {
      categories.value = (await getCategories()) || [];
    }
  }

  setIsLoading(false);
});

function setEvents(_events, sort = sortDirections.DESC) {
  const dateExtractor = (event: EventResponse) => event.eventStartTime;

  if (sort === sortDirections.DESC) {
    sortByDateInPlace(_events, dateExtractor, sortDirections.DESC);
  } else {
    sortByDateInPlace(_events, dateExtractor, sortDirections.ASC);
  }

  events.value = _events;
}

const eventToBeDeleted = ref<EventResponse>();

function startConfirmCancel(event: EventResponse) {
  eventToBeDeleted.value = event;
  isCancelConfirmModalOpen.value = true;
}

async function confirmCancelEvent() {
  const event = eventToBeDeleted.value;
  if (!event) {
    return;
  }

  const isSuccess = await deleteEvent(event.id);
  isCancelConfirmModalOpen.value = false;
  if (isSuccess) {
    events.value = events.value.filter((e) => e.id !== event.id);
    isCancelSuccessModalOpen.value = true;
  } else {
    isCancelErrorModalOpen.value = true;
  }
}

function openEventDetails(event: EventResponse) {
  selectedEvent.value = event;
}

function closeEventDetails() {
  selectedEvent.value = null;
}

async function saveEvent(updates: EditEventRequest, file: File | undefined) {
  if (!editingState.isEditing) {
    return;
  }

  const currentEvent = editingState.item;
  const selectedEventId = currentEvent.id;

  const newDate = new Date(updates.eventStartTime);
  if ((!isNaN(newDate.getTime()) && newDate.getTime() !== new Date(currentEvent.eventStartTime).getTime()) ||
    updates.eventNotes !== undefined && updates.eventNotes !== currentEvent.eventNotes ||
    file !== undefined) {
    console.log("updating event");
    console.log(newDate, new Date(currentEvent.eventStartTime));
    console.log(updates.eventNotes, currentEvent.eventNotes);

    const updatedEvent = await updateEvent(selectedEventId, updates, file);
    if (updatedEvent) {
      const event = events.value.find((e) => e.id === selectedEventId);
      event.eventStartTime = updatedEvent.eventStartTime;
      event.eventNotes = updatedEvent.eventNotes;
      event.files = updatedEvent.files;
      isEditSuccessModalOpen.value = true;
    } else {
      isEditErrorModalOpen.value = true;
    }
  }

  stopEditing();
}

async function filterEvents() {
  const categoryId = filter.value.categoryId;
  const date = filter.value.date;
  let _type = filter.value.type;
  const _filter: Record<string, unknown> = {
    categoryId,
  };

  // add startAt only if all type is selected
  if (date && _type === eventTypes.ALL) {
    const localDate = `${filter.value.date}T00:00:00`;
    const startAt = new Date(localDate);
    _filter.startAt = startAt.toISOString();
    _type = eventTypes.DAY;
  }

  if (_type !== eventTypes.ALL) {
    _filter.type = _type;
  }

  setIsLoading(true);
  const events = await getEventsByFilter(_filter);

  if (_type === eventTypes.UPCOMING || _type === eventTypes.DAY) {
    setEvents(events, sortDirections.ASC);
  } else {
    setEvents(events, sortDirections.DESC);
  }

  setIsLoading(false);
}

function handleCreateEventSuccess(event: EventResponse) {
  if (!isGuest.value) {
    events.value.push(event);
  }
  isCreateEventModalOpen.value = false;
  isCreateSuccessModalOpen.value = true;
}

function handleCreateEventError() {
  isCreateEventModalOpen.value = false;
  isCreateErrorModalOpen.value = true;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
type SlotProps = BaseSlotProps<EventResponse>;
</script>

<template>
  <PageLayout header="Events">
    <template #subheader>
      <div class="mb-4 flex justify-between">
        <div class="mb-4 mt-2">
          {{ filteredEvents.length }} events shown
        </div>

        <!-- Filter -->
        <div class="flex flex-wrap content-end gap-2">
          <div class="grid">
            <label class="text-xs text-slate-600">Search</label>
            <input
              v-model="search"
              type="text"
              class="self-baseline rounded-sm border border-gray-200 bg-white p-1 text-sm shadow-md shadow-gray-500/5"
              placeholder="Search by keyword"
            >
          </div>
        
          <div class="flex flex-col gap-1">
            <label class="text-xs text-slate-600">Category</label>
            <select
              v-model="filter.categoryId"
              class="self-baseline rounded-sm border border-gray-200 bg-white p-1 text-sm shadow-md shadow-gray-500/5"
              @change="filterEvents"
            >
              <option :value="categoryTypes.ALL">
                All
              </option>
              <option
                v-for="category in categories"
                :key="category.id"
                :value="category.id"
              >
                {{ category.eventCategoryName }}
              </option>
            </select>
          </div>

          <div class="flex">
            <div class="flex flex-col gap-1">
              <label class="text-xs text-slate-600">Type</label>
              <select
                v-model="filter.type"
                class="rounded-sm border border-gray-200 bg-white p-1 text-sm shadow-md shadow-gray-500/5"
                @change="filterEvents"
              >
                <option :value="eventTypes.ALL">
                  All
                </option>
                <option :value="eventTypes.UPCOMING">
                  Upcoming
                </option>
                <option :value="eventTypes.PAST">
                  Past
                </option>
              </select>
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs text-slate-600">Date</label>
              <input
                v-model="filter.date"
                class="rounded-sm border border-gray-200 bg-white p-1 text-sm shadow-md shadow-gray-500/5 disabled:bg-slate-200 disabled:text-slate-400"
                type="date"
                :disabled="filter.type !== eventTypes.ALL"
                :max="inputConstraits.MAX_DATE"
                @change="filterEvents"
              >
            </div>
            <div>
              <button
                type="reset"
                class="mt-5 flex  items-center  rounded text-sm text-indigo-500 hover:text-indigo-600 disabled:cursor-not-allowed"
                @click="resetFilter"
              >
                <span class="material-symbols-outlined">
                  close
                </span>
                Reset Filter
              </button>
            </div>
          </div>
        </div>

        <button
          v-if="!isLecturer"
          type="submit"
          class="mt-2 flex items-center gap-1 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-50"
          @click="isCreateEventModalOpen = true"
        >
          <span class="material-symbols-outlined">
            add
          </span>
          Create Event
        </button>
      </div>
    </template>

    <template #content>
      <div class="flex justify-start">
        <BaseTable
          :headers="[
            {
              name: 'Name',
              key: 'bookingName',
            },
            {
              name: 'Date & Time',
              key: 'eventStartTime',
              override: true,
            },
            {
              name: 'Category',
              key: 'eventCategory',
            },
          ]"
          :items="filteredEvents"
          :enable-edit="!isLecturer"
          :enable-delete="!isLecturer"
          :selected-key="editingState.isEditing && editingState.item.id.toString()"
          :key-extractor="(event) => event.id"
          :is-loading="isLoading"
          @edit="(event) => {
            closeEventDetails();
            startEditing(event);
          }"
          @delete="startConfirmCancel"
          @select="(event) => {
            withNoEditing(() => {
              openEventDetails(event);
            });
          }"
        >
          <template #cell:bookingName="{ item }: SlotProps">
            <span class="font-medium">{{ item.bookingName }}</span>
          </template>

          <template #cell:eventStartTime="{ item, dClass }: SlotProps">
            <td
              :class="dClass"
              class="w-80"
            >
              <div class="material-icon-settings flex flex-col">
                <div class="flex flex-col gap-1 text-sm">
                  <div class="flex gap-2">
                    <span class="material-symbols-outlined pt-1">
                      calendar_month
                    </span>
                    {{ formatDateAndFromToTime(new Date(item.eventStartTime), item.eventDuration) }}
                  </div>
                </div>
              </div>
            </td>
          </template>

          <template #cell:eventCategory="{ item }: SlotProps">
            <div class="flex">
              <Badge :text="item.eventCategory.eventCategoryName" />
            </div>
          </template>

          <template #empty>
            <span v-if="isAuthenticated">
              <span v-if="filter.type === eventTypes.UPCOMING">No On-Going or Upcoming Events</span>
              <span v-else-if="filter.type === eventTypes.PAST">No Past Events</span>
              <span v-else>No Scheduled Event</span>
            </span>
            <span v-else>
              Please <router-link
                to="/login"
                class="text-sky-500 underline"
              >login</router-link> to view events
            </span>
          </template>
        </BaseTable>

        <div
          v-if="editingState.isEditing || selectedEvent"
          class="relative w-4/12 bg-slate-100 p-4"
        >
          <EditEvent
            v-if="editingState.isEditing"
            class="sticky top-24"
            :current-event="editingState.item"
            @cancel="stopEditing"
            @save="saveEvent"
          />
          <EventDetails
            v-if="selectedEvent"
            class="sticky top-24"
            :current-event="selectedEvent"
            @close="closeEventDetails"
          />
        </div>
      </div>
    </template>
  </PageLayout>

  <Modal
    title="Success"
    subtitle="Event has been saved"
    :is-open="isEditSuccessModalOpen"
    @close="isEditSuccessModalOpen = false"
  />

  <Modal
    title="Error"
    subtitle="Something went wrong"
    button-text="Try Again"
    :is-open="isEditErrorModalOpen"
    variant="error"
    @close="isEditErrorModalOpen = false"
  />

  <Modal
    title="Success"
    subtitle="Event has been cancelled"
    :is-open="isCancelSuccessModalOpen"
    @close="isCancelSuccessModalOpen = false"
  />

  <Modal
    title="Error"
    subtitle="Something went wrong"
    button-text="Try Again"
    :is-open="isCancelErrorModalOpen"
    variant="error"
    @close="isCancelErrorModalOpen = false"
  />

  <Modal
    title="Are you sure?"
    subtitle="This action cannot be undone"
    type="confirm"
    button-cancel-text="Cancel"
    button-confirm-text="Confirm"
    variant="error"
    :is-open="isCancelConfirmModalOpen"
    @close="isCancelConfirmModalOpen = false"
    @confirm="confirmCancelEvent"
  />

  
  <Modal
    title="Success"
    subtitle="Event created successfully"
    :is-open="isCreateSuccessModalOpen"
    @close="isCreateSuccessModalOpen = false"
  />

  <Modal
    title="Error"
    subtitle="Something went wrong"
    button-text="Try Again"
    :is-open="isCreateErrorModalOpen"
    variant="error"
    @close="isCreateErrorModalOpen = false"
  />

  <CreateEventModal
    v-if="isCreateEventModalOpen"
    :is-open="true"
    @close="isCreateEventModalOpen = false"
    @success="handleCreateEventSuccess"
    @error="handleCreateEventError"
  />
</template>

<style scoped>
.material-icon-settings {
  font-variation-settings:
    'FILL' 0,
    'wght' 400,
    'GRAD' 0,
    'opsz' 48;
}

.material-symbols-outlined {
  font-size: 1.2rem;
}
</style>
