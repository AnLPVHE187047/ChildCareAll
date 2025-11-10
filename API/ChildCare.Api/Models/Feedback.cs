using System;
using System.Collections.Generic;

namespace ChildCare.Api.Models;

public partial class Feedback
{
    public int FeedbackId { get; set; }

    public int UserId { get; set; }

    public int? StaffId { get; set; }

    public int Rating { get; set; }

    public string? Comment { get; set; }
    public int? AppointmentId { get; set; }

    public DateTime? CreatedAt { get; set; }

    public virtual Staff? Staff { get; set; }

    public virtual User User { get; set; } = null!;
    public virtual Appointment? Appointment { get; set; }
}
