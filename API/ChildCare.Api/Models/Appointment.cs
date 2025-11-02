using System;
using System.Collections.Generic;

namespace ChildCare.Api.Models;

public partial class Appointment
{
    public int AppointmentId { get; set; }

    public int UserId { get; set; }

    public int ServiceId { get; set; }

    public int? StaffId { get; set; }

    public DateOnly AppointmentDate { get; set; }

    public TimeOnly AppointmentTime { get; set; }

    public string Address { get; set; } = null!;

    public string Status { get; set; } = null!;

    public DateTime? CreatedAt { get; set; }

    public virtual Service Service { get; set; } = null!;

    public virtual Staff? Staff { get; set; }

    public virtual User User { get; set; } = null!;
}
