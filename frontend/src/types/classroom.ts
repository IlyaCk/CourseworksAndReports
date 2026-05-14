export interface Announcement {
  alternateLink?: string;
  assigneeMode?: string;
  courseId?: string;
  creationTime?: string;
  creatorUserId?: string;
  id?: string;
  individualStudentsOptions?: IndividualStudentsOptions;
  materials?: Material[];
  scheduledTime?: string;
  state?: string;
  text?: string;
  updateTime?: string;
}
export interface Assignment {
  studentWorkFolder?: DriveFolder;
}
export interface AssignmentSubmission {
  attachments?: Attachment[];
}
export interface Attachment {
  driveFile?: DriveFile;
  form?: Form;
  link?: Link;
  youTubeVideo?: YouTubeVideo;
  isCourseWork?: boolean;
}
export interface CloudPubsubTopic {
  topicName?: string;
}
export interface Course {
  alternateLink?: string;
  calendarId?: string;
  courseGroupEmail?: string;
  courseMaterialSets?: CourseMaterialSet[];
  courseState?: string;
  creationTime?: string;
  description?: string;
  descriptionHeading?: string;
  enrollmentCode?: string;
  guardiansEnabled?: boolean;
  id?: string;
  name?: string;
  ownerId?: string;
  room?: string;
  section?: string;
  teacherFolder?: DriveFolder;
  teacherGroupEmail?: string;
  updateTime?: string;
}
export interface CourseAlias {
  alias?: string;
}
export interface CourseMaterial {
  // updated
  driveFile?: { driveFile: DriveFile };
  form?: Form;
  link?: Link;
  youTubeVideo?: YouTubeVideo;
}
export interface CourseMaterialSet {
  // updated
  materials: CourseMaterial[];
  title?: string;
}
export interface CourseRosterChangesInfo {
  courseId?: string;
}
export interface CourseWork {
  alternateLink?: string;
  assigneeMode?: string;
  assignment?: Assignment;
  associatedWithDeveloper?: boolean;
  courseId?: string;
  creationTime?: string;
  creatorUserId?: string;
  description?: string;
  dueDate?: Date;
  dueTime?: TimeOfDay;
  id?: string;
  individualStudentsOptions?: IndividualStudentsOptions;
  materials?: Material[];
  maxPoints?: number;
  multipleChoiceQuestion?: MultipleChoiceQuestion;
  scheduledTime?: string;
  state?: string;
  submissionModificationMode?: string;
  title?: string;
  updateTime?: string;
  workType?: string;
}
export interface CourseWorkChangesInfo {
  courseId?: string;
}
export interface Date {
  day?: number;
  month?: number;
  year?: number;
}
export interface DriveFile {
  alternateLink?: string;
  id?: string;
  thumbnailUrl?: string;
  title?: string;
}
export interface DriveFolder {
  alternateLink?: string;
  id?: string;
  title?: string;
}
export interface Feed {
  courseRosterChangesInfo?: CourseRosterChangesInfo;
  courseWorkChangesInfo?: CourseWorkChangesInfo;
  feedType?: string;
}
export interface Form {
  formUrl?: string;
  responseUrl?: string;
  thumbnailUrl?: string;
  title?: string;
}
export interface GlobalPermission {
  permission?: string;
}
export interface GradeHistory {
  actorUserId?: string;
  gradeChangeType?: string;
  gradeTimestamp?: string;
  maxPoints?: number;
  pointsEarned?: number;
}
export interface Guardian {
  guardianId?: string;
  guardianProfile?: UserProfile;
  invitedEmailAddress?: string;
  studentId?: string;
}
export interface GuardianInvitation {
  creationTime?: string;
  invitationId?: string;
  invitedEmailAddress?: string;
  state?: string;
  studentId?: string;
}
export interface IndividualStudentsOptions {
  studentIds?: string[];
}
export interface Invitation {
  courseId?: string;
  id?: string;
  role?: string;
  userId?: string;
}
export interface Link {
  thumbnailUrl?: string;
  title?: string;
  url?: string;
}
export interface ListAnnouncementsResponse {
  announcements?: Announcement[];
  nextPageToken?: string;
}
export interface ListCourseAliasesResponse {
  aliases?: CourseAlias[];
  nextPageToken?: string;
}
export interface ListCourseWorkResponse {
  courseWork?: CourseWork[];
  nextPageToken?: string;
}
export interface ListCoursesResponse {
  courses?: Course[];
  nextPageToken?: string;
}
export interface ListGuardianInvitationsResponse {
  guardianInvitations?: GuardianInvitation[];
  nextPageToken?: string;
}
export interface ListGuardiansResponse {
  guardians?: Guardian[];
  nextPageToken?: string;
}
export interface ListInvitationsResponse {
  invitations?: Invitation[];
  nextPageToken?: string;
}
export interface ListStudentSubmissionsResponse {
  nextPageToken?: string;
  studentSubmissions?: StudentSubmission[];
}
export interface ListStudentsResponse {
  nextPageToken?: string;
  students?: Student[];
}
export interface ListTeachersResponse {
  nextPageToken?: string;
  teachers?: Teacher[];
}
export interface Material {
  driveFile?: SharedDriveFile;
  form?: Form;
  link?: Link;
  youtubeVideo?: YouTubeVideo;
}
export interface ModifyAnnouncementAssigneesRequest {
  assigneeMode?: string;
  modifyIndividualStudentsOptions?: ModifyIndividualStudentsOptions;
}
export interface ModifyAttachmentsRequest {
  addAttachments?: Attachment[];
}
export interface ModifyCourseWorkAssigneesRequest {
  assigneeMode?: string;
  modifyIndividualStudentsOptions?: ModifyIndividualStudentsOptions;
}
export interface ModifyIndividualStudentsOptions {
  addStudentIds?: string[];
  removeStudentIds?: string[];
}
export interface MultipleChoiceQuestion {
  choices?: string[];
}
export interface MultipleChoiceSubmission {
  answer?: string;
}
export interface Name {
  familyName?: string;
  fullName?: string;
  givenName?: string;
}
export interface Registration {
  cloudPubsubTopic?: CloudPubsubTopic;
  expiryTime?: string;
  feed?: Feed;
  registrationId?: string;
}
export interface SharedDriveFile {
  driveFile?: DriveFile;
  shareMode?: string;
}
export interface ShortAnswerSubmission {
  answer?: string;
}
export interface StateHistory {
  actorUserId?: string;
  state?: string;
  stateTimestamp?: string;
}
export interface Student {
  courseId?: string;
  profile?: UserProfile;
  studentWorkFolder?: DriveFolder;
  userId?: string;
}
export interface StudentSubmission {
  alternateLink?: string;
  assignedGrade?: number;
  assignmentSubmission?: AssignmentSubmission;
  associatedWithDeveloper?: boolean;
  courseId?: string;
  courseWorkId?: string;
  courseWorkType?: string;
  creationTime?: string;
  draftGrade?: number;
  id?: string;
  late?: boolean;
  multipleChoiceSubmission?: MultipleChoiceSubmission;
  shortAnswerSubmission?: ShortAnswerSubmission;
  state?: string;
  submissionHistory?: SubmissionHistory[];
  updateTime?: string;
  userId?: string;
}
export interface SubmissionHistory {
  gradeHistory?: GradeHistory;
  stateHistory?: StateHistory;
}
export interface Teacher {
  courseId?: string;
  profile?: UserProfile;
  userId?: string;
}
export interface TimeOfDay {
  hours?: number;
  minutes?: number;
  nanos?: number;
  seconds?: number;
}
export interface UserProfile {
  emailAddress?: string;
  id?: string;
  name?: Name;
  permissions?: GlobalPermission[];
  photoUrl?: string;
  verifiedTeacher?: boolean;
}
export interface YouTubeVideo {
  alternateLink?: string;
  id?: string;
  thumbnailUrl?: string;
  title?: string;
}
