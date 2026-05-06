create table public.care_tasks (
  id uuid not null default gen_random_uuid (),
  user_id uuid null default gen_random_uuid (),
  saved_plant_id uuid null default gen_random_uuid (),
  task_type public.task_type_enum null,
  cadence_days numeric null,
  next_due_at timestamp with time zone null,
  last_completed_at timestamp with time zone null,
  enabled boolean null,
  user_override boolean null,
  updated_at timestamp with time zone null,
  created_at timestamp with time zone not null default now(),
  constraint care_tasks_pkey primary key (id),
  constraint care_tasks_saved_plant_id_fkey foreign KEY (saved_plant_id) references saved_plants (id) on update CASCADE on delete CASCADE,
  constraint care_tasks_user_id_fkey foreign KEY (user_id) references profiles (user_id) on delete CASCADE
) TABLESPACE pg_default;