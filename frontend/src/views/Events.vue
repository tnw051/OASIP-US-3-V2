<script setup>
import { onBeforeMount, ref } from "vue";
import Badge from "../components/Badge.vue";
import EditEvent from "../components/EditEvent.vue";
import EventDetails from "../components/EventDetails.vue";
import Modal from "../components/Modal.vue";
import Table from "../components/Table.vue";
import { deleteEvent, getCategories, getEvents, getEventsByFilter, updateEvent } from "../service/api";
import { formatDateTime, inputConstraits, sortByDateInPlace, sortDirections } from "../utils";
import { useIsLoading } from "../utils/useIsLoading";

const events = ref([]);
const currentEvent = ref({});
const categories = ref([]);
const { isLoading, setIsLoading } = useIsLoading(true);
const isEditSuccessModalOpen = ref(false);
const isEditErrorModalOpen = ref(false);
const isCancelSuccessModalOpen = ref(false);
const isCancelErrorModalOpen = ref(false);
const isCancelConfirmModalOpen = ref(false);

const eventTypes = {
  DAY: "day",
  UPCOMING: "upcoming",
  PAST: "past",
  ALL: null,
};

const categoryTypes = {
  ALL: null,
}

const filter = ref({
  categoryId: categoryTypes.ALL,
  type: eventTypes.ALL,
  date: ''
});

onBeforeMount(async () => {
  const events = await getEvents();
  setEvents(events);
  categories.value = await getCategories();
  setIsLoading(false);
});

function setEvents(_events, sort = sortDirections.DESC) {
  const dateExtractor = (event) => event.eventStartTime;

  if (sort === sortDirections.DESC) {
    sortByDateInPlace(_events, dateExtractor, sortDirections.DESC);
  } else {
    sortByDateInPlace(_events, dateExtractor, sortDirections.ASC);
  }

  events.value = _events;
}

const eventToBeDeleted = ref(null);

function startConfirmCancel(event) {
  eventToBeDeleted.value = event;
  isCancelConfirmModalOpen.value = true
}

async function confirmCancelEvent(event) {
  const isSuccess = await deleteEvent(event.id);
  isCancelConfirmModalOpen.value = false;
  if (isSuccess) {
    events.value = events.value.filter((e) => e.id !== event.id);
    isCancelSuccessModalOpen.value = true;
  } else {
    isCancelErrorModalOpen.value = true;
  }
}

function selectEvent(event) {
  if (isEditing.value) {
    return;
  }
  currentEvent.value = event;
}

const isEditing = ref(false)
function startEdit(event) {
  if (isEditing.value) {
    return;
  }
  currentEvent.value = event;
  isEditing.value = true;
}

function stopEdit() {
  currentEvent.value = {};
  isEditing.value = false;
}

async function saveEvent(updates) {
  const selectedEventId = currentEvent.value.id;

  if (new Date(updates.eventStartTime).getTime() !== new Date(currentEvent.value.eventStartTime).getTime() ||
    updates.eventNotes !== currentEvent.value.eventNotes) {
    const updatedEvent = await updateEvent(selectedEventId, updates);
    if (updatedEvent) {
      const event = events.value.find((e) => e.id === selectedEventId);
      event.eventStartTime = updatedEvent.eventStartTime;
      event.eventNotes = updatedEvent.eventNotes;
      isEditSuccessModalOpen.value = true;
    } else {
      isEditErrorModalOpen.value = true;
    }
  }

  isEditing.value = false;
}

async function filterEvents() {
  const categoryId = filter.value.categoryId;
  const date = filter.value.date;
  let _type = filter.value.type;
  const _filter = {
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
  const ascending = [eventTypes.UPCOMING, eventTypes.DAY];

  if (ascending.includes(_type)) {
    setEvents(events, sortDirections.ASC);
  } else {
    setEvents(events, sortDirections.DESC);
  }

  setIsLoading(false);
}
</script>

<template>
  <div class="py-8 px-12 max-w-[1440px] flex mx-auto">

    <div class="flex flex-col text-slate-700">
      <h1 class="font-semibold text-4xl">Events</h1>
      <div class="flex justify-between mb-4">
        <div class="mb-4 mt-2">{{ events.length }} events shown</div>
        <div class="flex gap-6 flex-wrap">

          <div class="flex flex-col gap-1">
            <label class="text-xs text-slate-600">Category</label>
            <select v-model="filter.categoryId"
              class="text-sm bg-white border border-gray-200 shadow-md shadow-gray-500/5 rounded-sm p-1 self-baseline"
              @change="filterEvents">
              <option :value="categoryTypes.ALL">All</option>
              <option v-for="category in categories" :value="category.id">{{ category.eventCategoryName }}</option>
            </select>
          </div>

          <div class="flex gap-2">
            <div class="flex flex-col gap-1">
              <label class="text-xs text-slate-600">Type</label>
              <select v-model="filter.type"
                class="text-sm bg-white border border-gray-200 shadow-md shadow-gray-500/5 rounded-sm p-1"
                @change="filterEvents">
                <option :value="eventTypes.ALL">All</option>
                <option :value="eventTypes.UPCOMING">Upcoming</option>
                <option :value="eventTypes.PAST">Past</option>
              </select>
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs text-slate-600">Date</label>
              <input v-model="filter.date"
                class="text-sm bg-white border border-gray-200 shadow-md shadow-gray-500/5 rounded-sm p-1 disabled:bg-slate-200 disabled:text-slate-400"
                type="date" @change="filterEvents" :disabled="filter.type !== eventTypes.ALL"
                :max="inputConstraits.MAX_DATE" />
            </div>
          </div>

        </div>
      </div>
      <div class="flex">
        <Table :headers="[
          {
            name: 'Name',
            key: 'bookingName',
          },
          {
            name: 'Date & Time',
            key: 'eventStartTime',
          },
          {
            name: 'Category',
            key: 'eventCategory',
          },
        ]" :items="events" enable-edit enable-delete @edit="startEdit" @delete="startConfirmCancel"
          @select="selectEvent" :selected-key="currentEvent.id" :key-extractor="(event) => event.id"
          :is-loading="isLoading">
          <template #cell:bookingName="{ item }">
            <span class="font-medium">{{ item.bookingName }}</span>
          </template>

          <template #cell:eventStartTime="{ item }">
            <div class="flex flex-col">
              <span class="">{{ formatDateTime(new Date(item.eventStartTime)) }}</span>
              <span class="text-sm text-slate-500">{{ item.eventDuration }} minutes</span>
            </div>
          </template>

          <template #cell:eventCategory="{ item }">
            <div class="flex">
              <Badge :text="item.eventCategory.eventCategoryName" />
            </div>
          </template>

          <template #empty>
            <span v-if="filter.type === eventTypes.UPCOMING">No On-Going or Upcoming Events</span>
            <span v-else-if="filter.type === eventTypes.PAST">No Past Events</span>
            <span v-else>No Scheduled Event</span>
          </template>
        </Table>

        <div class="p-4 bg-slate-100 relative w-4/12" v-if="currentEvent.id">
          <EditEvent class="sticky top-24" :currentEvent="currentEvent" @cancel="isEditing = false" v-if="isEditing"
            @save="saveEvent" />
          <EventDetails class="sticky top-24" :currentEvent="currentEvent" @close="currentEvent = {}" v-else />
        </div>
      </div>
    </div>
  </div>

  <Modal title="Success" subtitle="Event has been saved" :is-open="isEditSuccessModalOpen"
    @close="isEditSuccessModalOpen = false" />

  <Modal title="Error" subtitle="Something went wrong" button-text="Try Again" :is-open="isEditErrorModalOpen"
    variant="error" @close="isEditErrorModalOpen = false" />

  <Modal title="Success" subtitle="Event has been cancelled" :is-open="isCancelSuccessModalOpen"
    @close="isCancelSuccessModalOpen = false" />

  <Modal title="Error" subtitle="Something went wrong" button-text="Try Again" :is-open="isCancelErrorModalOpen"
    variant="error" @close="isCancelErrorModalOpen = false" />

  <Modal title="Are you sure?" subtitle="This action cannot be undone" type="confirm" button-cancel-text="Cancel"
    button-confirm-text="Confirm" variant="error" :is-open="isCancelConfirmModalOpen"
    @close="isCancelConfirmModalOpen = false" @confirm="confirmCancelEvent(eventToBeDeleted)" />
</template>

<style scoped>
</style>
